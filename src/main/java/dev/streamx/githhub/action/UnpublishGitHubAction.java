package dev.streamx.githhub.action;

import static dev.streamx.githhub.Constants.INGESTION_MESSAGE_KEY;
import static dev.streamx.githhub.Constants.STREAMX_INGESTION_TOKEN;
import static dev.streamx.githhub.Constants.STREAMX_INGESTION_URL;
import static dev.streamx.githhub.Constants.UNPUBLISH_EVENT_TYPE;

import com.streamx.clients.ingestion.StreamxClient;
import com.streamx.clients.ingestion.exceptions.StreamxClientException;
import com.streamx.clients.ingestion.publisher.Publisher;
import dev.streamx.exception.GitHubActionException;
import dev.streamx.exception.MissingRequiredInputException;
import dev.streamx.ingestion.payload.KeyPayload;
import io.cloudevents.CloudEvent;
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
      STREAMX_INGESTION_URL, UNPUBLISH_EVENT_TYPE, INGESTION_MESSAGE_KEY};

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
    String eventType = inputs.getRequired(UNPUBLISH_EVENT_TYPE);
    String streamxIngestionUrl = inputs.getRequired(STREAMX_INGESTION_URL);
    Optional<String> streamxIngestionToken = inputs.get(STREAMX_INGESTION_TOKEN);

    try (StreamxClient streamxClient = streamxClientProvider.createStreamxClient(
        streamxIngestionUrl, streamxIngestionToken)) {
      KeyPayload unpublishPayload = new KeyPayload(eventType, key);

      CloudEvent unpublishEvent = unpublishPayload.resolve();
      logMessageNotice(commands, unpublishEvent);

      Publisher publisher = streamxClient.newPublisher();
      publisher.send(unpublishEvent);
      commands.notice(String.format(PUBLISHING_SUCCESSFUL_MSG_FMT, key));
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
