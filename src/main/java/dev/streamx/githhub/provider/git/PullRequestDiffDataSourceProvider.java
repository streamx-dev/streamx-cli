package dev.streamx.githhub.provider.git;

import static dev.streamx.githhub.Constants.INGESTION_INCLUDE_PATTERNS;
import static dev.streamx.githhub.Constants.INGESTION_TYPE;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.streamx.clients.ingestion.publisher.Message;
import dev.streamx.exception.GitHubActionException;
import dev.streamx.githhub.Constants;
import dev.streamx.githhub.provider.AbstractSourceProvider;
import dev.streamx.githhub.utils.FilesUtils;
import dev.streamx.ingestion.IngestionPayload;
import dev.streamx.ingestion.payload.FilePayload;
import dev.streamx.ingestion.payload.KeyPayload;
import dev.streamx.ingestion.schema.SchemaProvider;
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
import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;
import org.kohsuke.github.GHEventPayload;
import org.kohsuke.github.GHEventPayload.PullRequest;

@ApplicationScoped
public class PullRequestDiffDataSourceProvider extends AbstractSourceProvider {

  public static final String NAME = "PullRequestDiffSourceProvider";
  private static final Logger log = Logger.getLogger(PullRequestDiffDataSourceProvider.class);

  @Inject
  GitService gitService;
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
  public List<JsonNode> createPayload(Inputs inputs, Context context, GHEventPayload eventPayload)
      throws GitHubActionException {
    if (eventPayload instanceof PullRequest pullRequest) {
      assertProviderRequiredInputParameters(inputs, Constants.STREAMX_INGESTION_URL,
          Constants.INGESTION_CHANNEL, INGESTION_INCLUDE_PATTERNS);

      try {
        int commits = pullRequest.getPullRequest().getCommits();
        String schemaType = getIngestionSchemaType(schemaProvider, inputs);
        String ingestionType = getInputString(inputs, INGESTION_TYPE);

        String workspace = getWorkspace(inputs, context.getGitHubWorkspace());

        String[] includePatterns = null;
        Optional<String> filePatternsInputOpt = inputs.get(INGESTION_INCLUDE_PATTERNS);
        if (filePatternsInputOpt.isPresent()) {
          includePatterns = objectMapper.readValue(filePatternsInputOpt.get(), String[].class);
        }
        DiffResult diffResult = gitService.getDiff(workspace, commits);
        return processDiffResult(diffResult, includePatterns, workspace, schemaType, ingestionType);
      } catch (IOException exc) {
        log.error(exc.getMessage(), exc);
        return Collections.emptyList();
      }
    } else {
      throw new IllegalArgumentException("Expected PullRequest eventPayload instance");
    }
  }

  private List<JsonNode> processDiffResult(DiffResult diffResult,
      String[] includePatterns, String workspace, String schemaType, String ingestionType) {
    List<JsonNode> result = new ArrayList<>();
    List<JsonNode> modifiedMessages = transformToIngestionMessages(
        diffResult.getModifiedPaths(),
        includePatterns,
        Message.PUBLISH_ACTION, workspace, schemaType, ingestionType);
    result.addAll(modifiedMessages);
    List<JsonNode> deletedMessages = transformToIngestionMessages(
        diffResult.getDeletedPaths(),
        includePatterns,
        Message.UNPUBLISH_ACTION, workspace, null, null);
    result.addAll(deletedMessages);
    return result;
  }

  private List<JsonNode> transformToIngestionMessages(Set<String> paths, String[] includePatterns,
      String action, String workspace, String schemaType, String ingestionType) {
    if (Objects.isNull(paths) || paths.isEmpty()) {
      return Collections.emptyList();
    }
    return paths.stream()
        .filter(path -> Objects.isNull(includePatterns) || FilesUtils.isValidPath(path,
            includePatterns))
        .map(path -> getIngestionPayload(action, workspace, path, schemaType, ingestionType))
        .map(fileMessage -> {
          try {
            return fileMessage.resolve();
          } catch (GitHubActionException exc) {
            log.error(exc.getMessage(), exc);
            return null;
          }
        })
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  private IngestionPayload getIngestionPayload(String action, String workspace,
      String path, String schemaType, String type) {
    if (StringUtils.equals(Message.UNPUBLISH_ACTION, action)) {
      return new KeyPayload(action, path);
    } else {
      FilePayload filePayload = new FilePayload(action, workspace, path, schemaType);
      if (Objects.nonNull(type)) {
        filePayload.setType(type);
      }
      return filePayload;
    }
  }

}
