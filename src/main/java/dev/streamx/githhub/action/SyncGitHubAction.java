package dev.streamx.githhub.action;

import static dev.streamx.githhub.Constants.INGESTION_CHANNEL;
import static dev.streamx.githhub.Constants.INGESTION_SOURCE_PROVIDER;
import static dev.streamx.githhub.Constants.STREAMX_INGESTION_URL;

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
      STREAMX_INGESTION_URL, INGESTION_CHANNEL, INGESTION_SOURCE_PROVIDER};

  @Action(SyncGitHubAction.ACTION_NAME)
  void syncAction(Commands commands, Inputs inputs, Context context,
      @PullRequest GHEventPayload.PullRequest payload) {
    commands.notice("Starting sync action");
    commonAction(commands, inputs, context, payload);
    commands.notice("Sync action has finished");
  }

  @Override
  String[] getActionRequiredInputParameters() {
    return ACTION_REQUIRED_INPUT_PARAMETERS;
  }
}