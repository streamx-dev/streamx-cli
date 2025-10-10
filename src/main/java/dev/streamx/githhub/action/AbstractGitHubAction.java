package dev.streamx.githhub.action;

import static dev.streamx.githhub.Constants.INGESTION_CHANNEL;
import static dev.streamx.githhub.Constants.INGESTION_SOURCE_PROVIDER;
import static dev.streamx.githhub.Constants.STREAMX_INGESTION_TOKEN;
import static dev.streamx.githhub.Constants.STREAMX_INGESTION_URL;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.streamx.clients.ingestion.StreamxClient;
import dev.streamx.clients.ingestion.exceptions.StreamxClientException;
import dev.streamx.clients.ingestion.publisher.FailureResult;
import dev.streamx.clients.ingestion.publisher.IngestionResult;
import dev.streamx.clients.ingestion.publisher.Publisher;
import dev.streamx.clients.ingestion.publisher.SuccessResult;
import dev.streamx.exception.GitHubActionException;
import dev.streamx.exception.MissingRequiredInputException;
import dev.streamx.githhub.provider.DataSourceProvider;
import dev.streamx.ingestion.IngestionConfig;
import dev.streamx.ingestion.StreamxClientProvider;
import io.quarkiverse.githubaction.Commands;
import io.quarkiverse.githubaction.Context;
import io.quarkiverse.githubaction.Inputs;
import io.quarkus.arc.All;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;
import org.kohsuke.github.GHEventPayload;

abstract class AbstractGitHubAction {

  protected static final String PUBLISHING_SUCCESSFUL_MSG_FMT =
      "Resource '%s' was sent successfully.";
  protected static final String PUBLISHING_UNSUCCESSFUL_MSG_FMT =
      "Resource publishing has failed with error: '%s' - %s.";
  private static final String MISSING_INPUT_PARAMETER_ERR_MSG_FMT =
      "Missing required %s input parameter. StreamX ingestion skipped.";
  private static final String UNSUPPORTED_DATA_SOURCE_PROVIDER_ERR_MSG_FMT =
      "Unsupported data source provider '%s'. StreamX ingestion skipped.";
  @Inject
  Logger log;
  @Inject
  StreamxClientProvider streamxClientProvider;
  @Inject
  IngestionConfig ingestionConfig;
  @Inject
  @All
  List<DataSourceProvider> dataSourceProviders;

  ObjectMapper objectMapper = new ObjectMapper();

  void commonAction(Commands commands, Inputs inputs, Context context)
      throws GitHubActionException {
    commonAction(commands, inputs, context, null);
  }

  void commonAction(Commands commands, Inputs inputs, Context context, GHEventPayload payload)
      throws GitHubActionException {
    try {
      assertRequiredInputParameters(inputs, getActionRequiredInputParameters());
    } catch (MissingRequiredInputException exc) {
      commands.error(exc.getMessage());
      throw exc;
    }

    String channel = inputs.getRequired(INGESTION_CHANNEL);
    String streamxIngestionUrl = inputs.getRequired(STREAMX_INGESTION_URL);
    Optional<String> streamxIngestionToken = inputs.get(STREAMX_INGESTION_TOKEN);

    try (StreamxClient streamxClient = streamxClientProvider.createStreamxClient(
        streamxIngestionUrl, streamxIngestionToken)) {
      DataSourceProvider dataSourceProvider = getDataSourceProvider(inputs);
      log.debugf("Using data source provider '%s'", dataSourceProvider.getName());
      List<JsonNode> ingestionPayload = dataSourceProvider.createPayload(inputs, context, payload);
      if (Objects.isNull(ingestionPayload) || ingestionPayload.isEmpty()) {
        log.info("No payload messages to publish.");
      } else {
        log.infof("Found %d payload messages for ingestion.", ingestionPayload.size());
        ingestionPayload.forEach(message -> logMessageNotice(commands, message));

        Publisher<JsonNode> publisher = streamxClient.newPublisher(channel, JsonNode.class);
        List<List<JsonNode>> chunkedPayload = chunkedPayload(ingestionPayload);
        chunkedPayload.forEach(partition -> {
          sendChunk(commands, publisher, partition);
        });
      }
    } catch (StreamxClientException exc) {
      String errMsg = "Failed to init StreamX publisher: " + exc.getMessage();
      log.error(errMsg, exc);
      commands.error(errMsg);
      throw new GitHubActionException(errMsg, exc);
    } catch (GitHubActionException exc) {
      log.error(exc.getMessage(), exc);
      commands.error(exc.getMessage());
      throw exc;
    }
  }

  private void sendChunk(Commands commands, Publisher<JsonNode> publisher,
      List<JsonNode> chunk) {
    try {
      List<IngestionResult> send = publisher.send(chunk);
      send.forEach(ingestionResult -> {
        SuccessResult successResult = ingestionResult.getSuccess();
        if (Objects.nonNull(successResult)) {
          logSuccessNotice(commands, successResult);
        } else {
          FailureResult failure = ingestionResult.getFailure();
          logErrorNotice(commands, failure);
        }
      });
    } catch (StreamxClientException exc) {
      String errMsg = "Failed to execute StreamX client: " + exc.getMessage();
      log.error(errMsg, exc);
      commands.error(errMsg);
    }
  }

  private List<List<JsonNode>> chunkedPayload(List<JsonNode> ingestionPayload)
      throws GitHubActionException {
    List<JsonNode> batch = new ArrayList<>();
    AtomicInteger batchSize = new AtomicInteger(0);

    List<List<JsonNode>> result = new ArrayList<>();
    Iterator<JsonNode> it = ingestionPayload.iterator();
    while (it.hasNext()) {
      JsonNode node = it.next();

      int size = calculateNodeSize(node);
      long batchSizeLimit = ingestionConfig.batchSourceProviderBatchSizeInBytes();
      if (size > batchSizeLimit) {
        String key = Optional.of(node).map(n -> n.get("key")).map(JsonNode::asText)
            .orElse("Unknown");
        log.debugf("Ingestion payload size of key [%s] exceeds limit of %d.",
            key, batchSizeLimit);
        continue;
      }
      if ((batchSize.get() + size) > batchSizeLimit) {
        result.add(batch);
        batch = new ArrayList<>();
        batchSize.set(0);
      }
      batch.add(node);
      batchSize.getAndAdd(size);
    }
    if (batchSize.get() > 0) {
      result.add(batch);
    }
    return result;
  }

  private int calculateNodeSize(JsonNode jsonNode) throws GitHubActionException {
    try {
      return objectMapper.writeValueAsBytes(jsonNode).length;
    } catch (JsonProcessingException exc) {
      throw new GitHubActionException("Failed to calculate node size: " + exc.getMessage(), exc);
    }
  }

  protected void logMessageNotice(Commands commands, JsonNode message) {
    String key = message.has("key")
        ? message.get("key").asText() : StringUtils.EMPTY;
    String action = message.has("action")
        ? message.get("action").asText() : StringUtils.EMPTY;
    commands.notice("Sending ingestion message: key='%s', action='%s'"
        .formatted(key, action));
  }

  abstract String[] getActionRequiredInputParameters();

  protected DataSourceProvider getDataSourceProvider(Inputs inputs) throws GitHubActionException {
    String sourceProviderName = Optional.of(inputs).map(i -> i.get(INGESTION_SOURCE_PROVIDER))
        .filter(Optional::isPresent).map(Optional::get).orElseThrow(
            () -> new MissingRequiredInputException(
                String.format(MISSING_INPUT_PARAMETER_ERR_MSG_FMT, INGESTION_SOURCE_PROVIDER)));
    return dataSourceProviders.stream()
        .filter(provider -> StringUtils.equals(sourceProviderName, provider.getName())).findFirst()
        .orElseThrow(() -> new UnsupportedOperationException(
            String.format(UNSUPPORTED_DATA_SOURCE_PROVIDER_ERR_MSG_FMT, sourceProviderName)));
  }


  protected void assertRequiredInputParameters(Inputs inputs, String... inputNames)
      throws MissingRequiredInputException {
    for (String inputName : inputNames) {
      Optional<String> inputOpt = inputs.get(inputName);
      if (inputOpt.isEmpty()) {
        throw new MissingRequiredInputException(
            String.format(MISSING_INPUT_PARAMETER_ERR_MSG_FMT, inputName));
      }
    }
  }

  protected void logSuccessNotice(Commands commands, SuccessResult result) {
    if (Objects.nonNull(result)) {
      String ingestionKey = result.getKey();
      commands.notice(String.format(PUBLISHING_SUCCESSFUL_MSG_FMT, ingestionKey));
    }
  }

  protected void logErrorNotice(Commands commands, FailureResult failure) {
    if (Objects.nonNull(failure)) {
      String errorCode = failure.getErrorCode();
      String errorMessage = failure.getErrorMessage();
      commands.error(String.format(PUBLISHING_UNSUCCESSFUL_MSG_FMT, errorCode, errorMessage));
    }
  }

}
