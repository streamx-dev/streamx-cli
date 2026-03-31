package dev.streamx.githhub.provider.git;

import static dev.streamx.githhub.Constants.INGESTION_INCLUDE_PATTERNS;
import static dev.streamx.githhub.Constants.PUBLISH_EVENT_TYPE;
import static dev.streamx.githhub.Constants.UNPUBLISH_EVENT_TYPE;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.streamx.exception.GitHubActionException;
import dev.streamx.githhub.Constants;
import dev.streamx.githhub.provider.AbstractSourceProvider;
import dev.streamx.githhub.utils.FilesUtils;
import dev.streamx.ingestion.IngestionPayload;
import dev.streamx.ingestion.payload.FilePayload;
import dev.streamx.ingestion.payload.KeyPayload;
import io.cloudevents.CloudEvent;
import io.quarkiverse.githubaction.Context;
import io.quarkiverse.githubaction.Inputs;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.jboss.logging.Logger;
import org.kohsuke.github.GHEventPayload;
import org.kohsuke.github.GHEventPayload.PullRequest;

@ApplicationScoped
public class PullRequestDiffDataSourceProvider extends AbstractSourceProvider {

  public static final String NAME = "PullRequestDiffSourceProvider";
  private static final Logger log = Logger.getLogger(PullRequestDiffDataSourceProvider.class);

  @Inject
  GitService gitService;

  ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public ObjectMapper getMapper() {
    return objectMapper;
  }

  @Override
  public List<CloudEvent> createPayload(Inputs inputs, Context context,
      GHEventPayload eventPayload)
      throws GitHubActionException {
    if (eventPayload instanceof PullRequest pullRequest) {
      assertProviderRequiredInputParameters(inputs, Constants.STREAMX_INGESTION_URL,
          Constants.PUBLISH_EVENT_TYPE, Constants.UNPUBLISH_EVENT_TYPE,
          INGESTION_INCLUDE_PATTERNS);

      try {
        int commits = pullRequest.getPullRequest().getCommits();
        String publishEventType = getInputString(inputs, PUBLISH_EVENT_TYPE);
        String unpublishEventType = getInputString(inputs, UNPUBLISH_EVENT_TYPE);

        String workspace = getWorkspace(inputs, context.getGitHubWorkspace());

        String[] includePatterns = null;
        Optional<String> filePatternsInputOpt = inputs.get(INGESTION_INCLUDE_PATTERNS);
        if (filePatternsInputOpt.isPresent()) {
          includePatterns = objectMapper.readValue(filePatternsInputOpt.get(), String[].class);
        }
        DiffResult diffResult = gitService.getDiff(workspace, commits);
        return processDiffResult(diffResult, includePatterns, workspace,
            publishEventType, unpublishEventType);
      } catch (IOException exc) {
        log.error(exc.getMessage(), exc);
        return Collections.emptyList();
      }
    } else {
      throw new IllegalArgumentException("Expected PullRequest eventPayload instance");
    }
  }

  private List<CloudEvent> processDiffResult(DiffResult diffResult,
      String[] includePatterns, String workspace,
      String publishEventType, String unpublishEventType) {
    List<CloudEvent> result = new ArrayList<>();
    List<CloudEvent> modifiedMessages = transformToCloudEvents(
        diffResult.getModifiedPaths(),
        includePatterns, workspace, publishEventType, true);
    result.addAll(modifiedMessages);
    List<CloudEvent> deletedMessages = transformToCloudEvents(
        diffResult.getDeletedPaths(),
        includePatterns, workspace, unpublishEventType, false);
    result.addAll(deletedMessages);
    return result;
  }

  private List<CloudEvent> transformToCloudEvents(Set<String> paths, String[] includePatterns,
      String workspace, String eventType, boolean isPublish) {
    if (Objects.isNull(paths) || paths.isEmpty()) {
      return Collections.emptyList();
    }
    return paths.stream()
        .filter(path -> Objects.isNull(includePatterns) || FilesUtils.isValidPath(path,
            includePatterns))
        .map(path -> getIngestionPayload(isPublish, workspace, path, eventType))
        .map(payload -> {
          try {
            return payload.resolve();
          } catch (GitHubActionException exc) {
            log.error(exc.getMessage(), exc);
            return null;
          }
        })
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  private IngestionPayload getIngestionPayload(boolean isPublish, String workspace,
      String path, String eventType) {
    if (!isPublish) {
      return new KeyPayload(eventType, path);
    } else {
      return new FilePayload(eventType, workspace, path);
    }
  }

}
