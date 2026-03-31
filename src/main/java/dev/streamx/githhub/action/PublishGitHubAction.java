package dev.streamx.githhub.action;

import static dev.streamx.githhub.Constants.INGESTION_SOURCE_PROVIDER;
import static dev.streamx.githhub.Constants.PUBLISH_EVENT_TYPE;
import static dev.streamx.githhub.Constants.STREAMX_INGESTION_URL;

import dev.streamx.exception.GitHubActionException;
import io.quarkiverse.githubaction.Action;
import io.quarkiverse.githubaction.Commands;
import io.quarkiverse.githubaction.Context;
import io.quarkiverse.githubaction.Inputs;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PublishGitHubAction extends AbstractGitHubAction {

  public static final String ACTION_NAME = "publish";

  private static final String[] ACTION_REQUIRED_INPUT_PARAMETERS = new String[]{
      STREAMX_INGESTION_URL, PUBLISH_EVENT_TYPE, INGESTION_SOURCE_PROVIDER};

  @Action(PublishGitHubAction.ACTION_NAME)
  void publishAction(Commands commands, Inputs inputs, Context context)
      throws GitHubActionException {
    commands.notice("Starting publish to StreamX action");
    commonAction(commands, inputs, context);
    commands.notice("Publish to StreamX action has finished");
  }

  @Override
  String[] getActionRequiredInputParameters() {
    return ACTION_REQUIRED_INPUT_PARAMETERS;
  }
}
