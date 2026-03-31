package dev.streamx.githhub.provider.common;

import static dev.streamx.githhub.Constants.PUBLISH_EVENT_TYPE;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.streamx.exception.GitHubActionException;
import dev.streamx.githhub.Constants;
import dev.streamx.githhub.provider.AbstractSourceProvider;
import dev.streamx.githhub.utils.FilesUtils;
import dev.streamx.ingestion.payload.FilePayload;
import io.cloudevents.CloudEvent;
import io.quarkiverse.githubaction.Context;
import io.quarkiverse.githubaction.Inputs;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.jboss.logging.Logger;
import org.kohsuke.github.GHEventPayload;

@ApplicationScoped
public class BatchSourceProvider extends AbstractSourceProvider {

  public static final String NAME = "BatchSourceProvider";

  @Inject
  Logger log;

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
  public List<CloudEvent> createPayload(Inputs inputs, Context context, GHEventPayload payload)
      throws GitHubActionException {
    assertProviderRequiredInputParameters(inputs, Constants.STREAMX_INGESTION_URL,
        Constants.PUBLISH_EVENT_TYPE, Constants.INGESTION_INCLUDE_PATTERNS);
    try {
      String eventType = getInputString(inputs, PUBLISH_EVENT_TYPE);
      String workspace = getWorkspace(inputs, context.getGitHubWorkspace());
      String[] includePatterns = getIncludePatterns(inputs);
      if (log.isDebugEnabled()) {
        log.debug("Creating ingestion payload for options:");
        log.debug("event type: " + eventType);
        log.debug("workspace: " + workspace);
        log.debug("include patterns: " + includePatterns);
      }
      Set<String> paths = FilesUtils.listFilteredFiles(workspace, includePatterns);
      return transformToCloudEvents(paths, eventType, workspace);
    } catch (IOException exc) {
      log.error(exc.getMessage(), exc);
      return Collections.emptyList();
    }
  }

  private List<CloudEvent> transformToCloudEvents(Set<String> paths,
      String eventType, String workspace) {
    if (Objects.isNull(paths) || paths.isEmpty()) {
      return Collections.emptyList();
    }
    return paths.stream()
        .map(path -> {
          try {
            return new FilePayload(eventType, workspace, path).resolve();
          } catch (GitHubActionException exc) {
            log.error(exc.getMessage(), exc);
            return null;
          }
        })
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

}
