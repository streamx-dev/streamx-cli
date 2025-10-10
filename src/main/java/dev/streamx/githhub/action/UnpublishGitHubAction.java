package dev.streamx.githhub.action;

import static dev.streamx.githhub.Constants.INGESTION_CHANNEL;
import static dev.streamx.githhub.Constants.INGESTION_MESSAGE_KEY;
import static dev.streamx.githhub.Constants.STREAMX_INGESTION_TOKEN;
import static dev.streamx.githhub.Constants.STREAMX_INGESTION_URL;

import com.fasterxml.jackson.databind.JsonNode;
import dev.streamx.clients.ingestion.StreamxClient;
import dev.streamx.clients.ingestion.exceptions.StreamxClientException;
import dev.streamx.clients.ingestion.publisher.Message;
import dev.streamx.clients.ingestion.publisher.Publisher;
import dev.streamx.clients.ingestion.publisher.SuccessResult;
import dev.streamx.exception.GitHubActionException;
import dev.streamx.exception.MissingRequiredInputException;
import dev.streamx.ingestion.payload.KeyPayload;
import io.quarkiverse.githubaction.Action;
import io.quarkiverse.githubaction.Commands;
import io.quarkiverse.githubaction.Inputs;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Optional;
import org.jboss.logging.Logger;

@ApplicationScoped
public class UnpublishGitHubAction extends AbstractGitHubAction {

  public static final String ACTION_NAME = "unpublish";

  private static final String[] ACTION_REQUIRED_INPUT_PARAMETERS = new String[]{
      STREAMX_INGESTION_URL, INGESTION_CHANNEL, INGESTION_MESSAGE_KEY};

  @Inject
  Logger log;

  @Action(UnpublishGitHubAction.ACTION_NAME)
  void unpublishAction(Commands commands, Inputs inputs) throws GitHubActionException {
    commands.notice("Starting unpublish from StreamX action");
    try {
      assertRequiredInputParameters(inputs, getActionRequiredInputParameters());
    } catch (MissingRequiredInputException exc) {
      commands.error(exc.getMessage());
      throw exc;
    }

    String key = inputs.getRequired(INGESTION_MESSAGE_KEY);
    String channel = inputs.getRequired(INGESTION_CHANNEL);
    String streamxIngestionUrl = inputs.getRequired(STREAMX_INGESTION_URL);
    Optional<String> streamxIngestionToken = inputs.get(STREAMX_INGESTION_TOKEN);

    try (StreamxClient streamxClient = streamxClientProvider.createStreamxClient(
        streamxIngestionUrl, streamxIngestionToken)) {
      KeyPayload unpublishPayload = new KeyPayload(Message.UNPUBLISH_ACTION, key);

      JsonNode unpublishMessage = unpublishPayload.resolve();
      logMessageNotice(commands, unpublishMessage);

      Publisher<JsonNode> publisher = streamxClient.newPublisher(channel, JsonNode.class);
      SuccessResult successResult = publisher.send(unpublishMessage);
      logSuccessNotice(commands, successResult);
    } catch (GitHubActionException exc) {
      log.error(exc.getMessage(), exc);
      commands.error(exc.getMessage());
      throw exc;
    } catch (StreamxClientException exc) {
      String errMsg = "Failed to execute StreamX client: " + exc.getMessage();
      log.error(errMsg, exc);
      commands.error(errMsg);
      throw new GitHubActionException(errMsg, exc);
    }
    commands.notice("Unpublish from StreamX action has finished");
  }

  @Override
  String[] getActionRequiredInputParameters() {
    return ACTION_REQUIRED_INPUT_PARAMETERS;
  }
}
