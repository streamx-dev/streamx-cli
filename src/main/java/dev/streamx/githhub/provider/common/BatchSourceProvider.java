package dev.streamx.githhub.provider.common;

import static dev.streamx.githhub.Constants.INGESTION_TYPE;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.streamx.clients.ingestion.publisher.Message;
import dev.streamx.exception.GitHubActionException;
import dev.streamx.githhub.Constants;
import dev.streamx.githhub.provider.AbstractSourceProvider;
import dev.streamx.githhub.utils.FilesUtils;
import dev.streamx.ingestion.payload.FilePayload;
import dev.streamx.ingestion.schema.SchemaProvider;
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
  @Inject
  SchemaProvider schemaProvider;

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
  public List<JsonNode> createPayload(Inputs inputs, Context context, GHEventPayload payload)
      throws GitHubActionException {
    assertProviderRequiredInputParameters(inputs, Constants.STREAMX_INGESTION_URL,
        Constants.INGESTION_CHANNEL, Constants.INGESTION_INCLUDE_PATTERNS);
    try {
      String schemaType = getIngestionSchemaType(schemaProvider, inputs);
      String ingestionType = getInputString(inputs, INGESTION_TYPE);
      String workspace = getWorkspace(inputs, context.getGitHubWorkspace());
      String[] includePatterns = getIncludePatterns(inputs);
      if (log.isDebugEnabled()) {
        log.debug("Creating ingestion payload for options:");
        log.debug("schema type: " + schemaType);
        log.debug("type: " + ingestionType);
        log.debug("workspace: " + workspace);
        log.debug("include patterns: " + includePatterns);
      }
      Set<String> paths = FilesUtils.listFilteredFiles(workspace, includePatterns);
      return tranformToIngestionMessages(paths, Message.PUBLISH_ACTION, workspace,
          schemaType, ingestionType);
    } catch (IOException exc) {
      log.error(exc.getMessage(), exc);
      return Collections.emptyList();
    }
  }

  private List<JsonNode> tranformToIngestionMessages(Set<String> paths,
      String action, String workspace, String schemaType, String ingestionType) {
    if (Objects.isNull(paths) || paths.isEmpty()) {
      return Collections.emptyList();
    }
    return paths.stream()
        .map(path -> new FilePayload(action, workspace, path, schemaType))
        .map(payload -> {
          if (Objects.nonNull(ingestionType)) {
            payload.setType(ingestionType);
          }
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


}
