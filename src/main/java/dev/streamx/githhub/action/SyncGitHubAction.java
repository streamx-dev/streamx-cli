package dev.streamx.githhub.action;

import static dev.streamx.githhub.Constants.INGESTION_SOURCE_PROVIDER;
import static dev.streamx.githhub.Constants.PUBLISH_EVENT_TYPE;
import static dev.streamx.githhub.Constants.STREAMX_INGESTION_URL;
import static dev.streamx.githhub.Constants.UNPUBLISH_EVENT_TYPE;

import dev.streamx.exception.GitHubActionException;
import io.quarkiverse.githubaction.Action;
import io.quarkiverse.githubaction.Commands;
import io.quarkiverse.githubaction.Context;
import io.quarkiverse.githubaction.Inputs;
import io.quarkiverse.githubapp.event.PullRequest;
import jakarta.enterprise.context.ApplicationScoped;
import org.kohsuke.github.GHEventPayload;

@ApplicationScoped
public class SyncGitHubAction extends AbstractGitHubAction {

  public static final String ACTION_NAME = "sync";

  private static final String[] ACTION_REQUIRED_INPUT_PARAMETERS = new String[]{
      STREAMX_INGESTION_URL, PUBLISH_EVENT_TYPE, UNPUBLISH_EVENT_TYPE,
      INGESTION_SOURCE_PROVIDER};

  @Action(SyncGitHubAction.ACTION_NAME)
  void syncAction(Commands commands, Inputs inputs, Context context,
      @PullRequest GHEventPayload.PullRequest payload) throws GitHubActionException {
    commands.notice("Starting sync action");
    commonAction(commands, inputs, context, payload);
    commands.notice("Sync action has finished");
  }

  @Override
  String[] getActionRequiredInputParameters() {
    return ACTION_REQUIRED_INPUT_PARAMETERS;
  }
}
