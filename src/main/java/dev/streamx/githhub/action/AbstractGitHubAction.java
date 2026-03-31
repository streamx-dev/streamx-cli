package dev.streamx.githhub.action;

import static dev.streamx.githhub.Constants.INGESTION_SOURCE_PROVIDER;
import static dev.streamx.githhub.Constants.STREAMX_INGESTION_TOKEN;
import static dev.streamx.githhub.Constants.STREAMX_INGESTION_URL;

import com.streamx.clients.ingestion.StreamxClient;
import com.streamx.clients.ingestion.exceptions.StreamxClientException;
import com.streamx.clients.ingestion.publisher.Publisher;
import dev.streamx.exception.GitHubActionException;
import dev.streamx.exception.MissingRequiredInputException;
import dev.streamx.githhub.provider.DataSourceProvider;
import dev.streamx.ingestion.IngestionConfig;
import dev.streamx.ingestion.StreamxClientProvider;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.format.EventFormat;
import io.cloudevents.jackson.JsonFormat;
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
import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;
import org.kohsuke.github.GHEventPayload;

abstract class AbstractGitHubAction {

  protected static final String PUBLISHING_SUCCESSFUL_MSG_FMT =
      "Resource '%s' was sent successfully.";
  private static final String MISSING_INPUT_PARAMETER_ERR_MSG_FMT =
      "Missing required %s input parameter. StreamX ingestion skipped.";
  private static final String UNSUPPORTED_DATA_SOURCE_PROVIDER_ERR_MSG_FMT =
      "Unsupported data source provider '%s'. StreamX ingestion skipped.";

  private static final EventFormat EVENT_FORMAT = new JsonFormat();

  @Inject
  Logger log;
  @Inject
  StreamxClientProvider streamxClientProvider;
  @Inject
  IngestionConfig ingestionConfig;
  @Inject
  @All
  List<DataSourceProvider> dataSourceProviders;

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

    String streamxIngestionUrl = inputs.getRequired(STREAMX_INGESTION_URL);
    Optional<String> streamxIngestionToken = inputs.get(STREAMX_INGESTION_TOKEN);

    try (StreamxClient streamxClient = streamxClientProvider.createStreamxClient(
        streamxIngestionUrl, streamxIngestionToken)) {
      DataSourceProvider dataSourceProvider = getDataSourceProvider(inputs);
      log.debugf("Using data source provider '%s'", dataSourceProvider.getName());
      List<CloudEvent> ingestionPayload = dataSourceProvider.createPayload(inputs, context,
          payload);
      if (Objects.isNull(ingestionPayload) || ingestionPayload.isEmpty()) {
        log.info("No payload messages to publish.");
      } else {
        log.infof("Found %d payload messages for ingestion.", ingestionPayload.size());
        ingestionPayload.forEach(event -> logMessageNotice(commands, event));

        Publisher publisher = streamxClient.newPublisher();
        List<List<CloudEvent>> chunkedPayload = chunkedPayload(ingestionPayload);
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

  private void sendChunk(Commands commands, Publisher publisher,
      List<CloudEvent> chunk) {
    try {
      publisher.send(chunk);
      chunk.forEach(event -> {
        String subject = event.getSubject();
        if (subject != null) {
          commands.notice(String.format(PUBLISHING_SUCCESSFUL_MSG_FMT, subject));
        }
      });
    } catch (StreamxClientException exc) {
      String errMsg = "Failed to execute StreamX client: " + exc.getMessage();
      log.error(errMsg, exc);
      commands.error(errMsg);
    }
  }

  private List<List<CloudEvent>> chunkedPayload(List<CloudEvent> ingestionPayload)
      throws GitHubActionException {
    List<CloudEvent> batch = new ArrayList<>();
    int batchSize = 0;

    List<List<CloudEvent>> result = new ArrayList<>();
    Iterator<CloudEvent> it = ingestionPayload.iterator();
    while (it.hasNext()) {
      CloudEvent event = it.next();

      int size = calculateEventSize(event);
      long batchSizeLimit = ingestionConfig.batchSourceProviderBatchSizeInBytes();
      if (size > batchSizeLimit) {
        String subject = Optional.ofNullable(event.getSubject()).orElse("Unknown");
        log.debugf("Ingestion payload size of subject [%s] exceeds limit of %d.",
            subject, batchSizeLimit);
        continue;
      }
      if ((batchSize + size) > batchSizeLimit) {
        result.add(batch);
        batch = new ArrayList<>();
        batchSize = 0;
      }
      batch.add(event);
      batchSize += size;
    }
    if (batchSize > 0) {
      result.add(batch);
    }
    return result;
  }

  private int calculateEventSize(CloudEvent event) throws GitHubActionException {
    byte[] serialized = EVENT_FORMAT.serialize(event);
    if (serialized == null) {
      throw new GitHubActionException("Failed to serialize CloudEvent for size calculation");
    }
    return serialized.length;
  }

  protected void logMessageNotice(Commands commands, CloudEvent event) {
    String subject = event.getSubject() != null ? event.getSubject() : StringUtils.EMPTY;
    String type = event.getType() != null ? event.getType() : StringUtils.EMPTY;
    commands.notice("Sending ingestion message: subject='%s', type='%s'"
        .formatted(subject, type));
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

}
