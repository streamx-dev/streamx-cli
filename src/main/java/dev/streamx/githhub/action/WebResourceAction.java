package dev.streamx.githhub.action;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import dev.streamx.clients.ingestion.StreamxClient;
import dev.streamx.clients.ingestion.exceptions.StreamxClientException;
import dev.streamx.clients.ingestion.publisher.Message;
import dev.streamx.clients.ingestion.publisher.Publisher;
import dev.streamx.clients.ingestion.publisher.SuccessResult;
import dev.streamx.exception.GitHubActionException;
import dev.streamx.githhub.git.GitService;
import dev.streamx.githhub.git.impl.DiffResult;
import dev.streamx.githhub.utils.FilesUtils;
import dev.streamx.ingestion.IngestionMessageJsonFactory;
import dev.streamx.ingestion.StreamxClientProvider;
import dev.streamx.ingestion.payload.RawPayload;
import dev.streamx.ingestion.payload.WebResourcePayload;
import io.quarkiverse.githubaction.Action;
import io.quarkiverse.githubaction.Commands;
import io.quarkiverse.githubaction.Context;
import io.quarkiverse.githubaction.Inputs;
import io.quarkiverse.githubapp.event.PullRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.jboss.logging.Logger;
import org.kohsuke.github.GHEventPayload;

@ApplicationScoped
public class WebResourceAction {

  static final String STREAMX_INGESTION_URL = "streamx-ingestion-url";
  static final String STREAMX_INGESTION_TOKEN = "streamx-ingestion-token";
  static final String STREAMX_INGESTION_WEBRESOURCE_INCLUDES = "streamx-ingestion-webresource-includes";

  private static final Logger log = Logger.getLogger(WebResourceAction.class);
  private static final String WEB_RESOURCE_SCHEMA_TYPE = "dev.streamx.blueprints.data.WebResource";
  private static final String WEB_RESOURCES_CHANNEL = "web-resources";
  private static final String BYTES_CONTENT_NODE_NAME = "bytes";
  private static final String CONTENT_NODE_NAME = "content";
  private static final Map<String, String> PUBLISH_PROPERTIES = Map.of("sx:type",
      "web-resource/static");

  private StreamxClientProvider streamxClientProvider;

  private GitService gitService;

  private ObjectMapper objectMapper;

  public WebResourceAction() {
  }

  @Inject
  public WebResourceAction(StreamxClientProvider streamxClientProvider,
      GitService gitService,
      ObjectMapper objectMapper) {
    this.streamxClientProvider = streamxClientProvider;
    this.gitService = gitService;
    this.objectMapper = objectMapper;
  }

  @Action("webresource_pull_request")
  void webresourceSyncOnPullRequestMerged(Commands commands, Inputs inputs,
      @PullRequest GHEventPayload.PullRequest payload, Context context) throws IOException {
    Optional<String> filePatternsInputOpt = inputs.get(STREAMX_INGESTION_WEBRESOURCE_INCLUDES);
    if (filePatternsInputOpt.isEmpty()) {
      commands.error(
          "Missing required STREAMX_INGESTION_WEBRESOURCE_INCLUDES variable. StreamX ingestion skipped.");
      return;
    }
    Optional<String> streamxIngestionUrl = inputs.get(STREAMX_INGESTION_URL);
    if (streamxIngestionUrl.isEmpty()) {
      commands.error(
          "Missing required STREAMX_INGESTION_BASE_URL variable. StreamX ingestion skipped.");
      return;
    }
    Optional<String> streamxToken = inputs.get(STREAMX_INGESTION_TOKEN);

    int commits = payload.getPullRequest().getCommits();
    commands.notice("Number of commits: " + commits);

    String workspace = context.getGitHubWorkspace();
    commands.notice("Workspace: " + workspace);

    try (StreamxClient streamxClient = streamxClientProvider.createStreamxClient(
        streamxIngestionUrl.get(), streamxToken)) {
      String[] filePatterns = objectMapper.readValue(filePatternsInputOpt.get(), String[].class);

      DiffResult diffResult = gitService.getDiff(workspace, commits);
      if (diffResult.isEmpty()) {
        commands.notice(String.format("No changes detected in workspace: %s", workspace));
      } else {
        Set<String> modifiedPaths = diffResult.getModifiedPaths();
        commands.notice(String.format("Changes detected in workspace: %s", workspace));
        commands.notice(String.format("%d modification(s) detected.", modifiedPaths.size()));
        if (!modifiedPaths.isEmpty()) {
          Publisher<JsonNode> publisher = streamxClient.newPublisher(getChannel(),
              JsonNode.class);
          for (String modifiedFile : modifiedPaths) {
            if (FilesUtils.isValidPath(modifiedFile, filePatterns)) {
              try {
                SuccessResult result = publishFile(publisher, workspace, modifiedFile);
                commands.notice(String.format("Resource '%s' was published successfully.",
                    result.getKey()));
              } catch (StreamxClientException exc) {
                commands.error(String.format("Failed to publish resource '%s'. Error: %s",
                    modifiedFile, exc.getMessage()));
              }
            }
          }
        }
        Set<String> deletedPaths = diffResult.getDeletedPaths();
        commands.notice(String.format("%d deletion(s) detected.", deletedPaths.size()));
        if (!deletedPaths.isEmpty()) {
          Publisher<JsonNode> publisher = streamxClient.newPublisher(getChannel(),
              JsonNode.class);
          for (String deletedFile : deletedPaths) {
            if (FilesUtils.isValidPath(deletedFile, filePatterns)) {
              try {
                SuccessResult result = publisher.unpublish(deletedFile);
                commands.notice(String.format("Unpublishing resource %s was successful",
                    result.getKey()));
              } catch (StreamxClientException exc) {
                commands.error(String.format("Failed to unpublish resource '%s'. Error: %s",
                    deletedFile, exc.getMessage()));
              }
            }
          }
        }
      }
    } catch (GitHubActionException exc) {
      log.error(exc.getMessage(), exc);
      commands.error(exc.getMessage());
    } catch (StreamxClientException exc) {
      String errMsg = "Failed to execute StreamX client: " + exc.getMessage();
      log.error(errMsg, exc);
      commands.error(errMsg);
    }
  }

  @Action("webresource_workflow_dispatch")
  void publishAll(Commands commands, Inputs inputs, Context context) {
    Optional<String> filePatternsInputOpt = inputs.get(STREAMX_INGESTION_WEBRESOURCE_INCLUDES);
    if (filePatternsInputOpt.isEmpty()) {
      commands.error(
          "Missing required STREAMX_INGESTION_WEBRESOURCE_INCLUDES variable. StreamX ingestion skipped.");
      return;
    }
    Optional<String> streamxIngestionUrl = inputs.get(STREAMX_INGESTION_URL);
    if (streamxIngestionUrl.isEmpty()) {
      commands.error(
          "Missing required STREAMX_INGESTION_BASE_URL variable. StreamX ingestion skipped.");
      return;
    }
    Optional<String> streamxToken = inputs.get(STREAMX_INGESTION_TOKEN);

    String workspace = context.getGitHubWorkspace();
    commands.notice("Workspace: " + workspace);

    try (StreamxClient streamxClient = streamxClientProvider.createStreamxClient(
        streamxIngestionUrl.get(), streamxToken)) {
      String[] filePatterns = objectMapper.readValue(filePatternsInputOpt.get(), String[].class);
      Set<String> files = FilesUtils.listFilteredFiles(workspace, filePatterns);
      if (!files.isEmpty()) {
        Publisher<JsonNode> publisher = streamxClient.newPublisher(getChannel(),
            JsonNode.class);
        for (String filePath : files) {
          try {
            SuccessResult result = publishFile(publisher, workspace, filePath);
            commands.notice(String.format("Resource '%s' was published successfully.",
                result.getKey()));
          } catch (StreamxClientException exc) {
            commands.error(String.format("Failed to publish resource '%s'. Error: %s",
                filePath, exc.getMessage()));

          }
        }
      }
    } catch (GitHubActionException exc) {
      log.error(exc.getMessage(), exc);
      commands.error(exc.getMessage());
    } catch (JacksonException exc) {
      String errMsg = "Failed to deserialize file patterns: " + exc.getMessage();
      log.error(errMsg, exc);
      commands.error(errMsg);
    } catch (StreamxClientException exc) {
      String errMsg = "Failed to execute StreamX client: " + exc.getMessage();
      log.error(errMsg, exc);
      commands.error(errMsg);
    }
  }

  private SuccessResult publishFile(Publisher<JsonNode> publisher, String workspace,
      String filePath) throws StreamxClientException, GitHubActionException {
    if (log.isDebugEnabled()) {
      log.debug(String.format("Publishing file '%s' from workspace '%s'.", filePath, workspace));
    }
    WebResourcePayload webResourcePayload = getWebResourcePayload(workspace, filePath);
    JsonNode message = prepareIngestionMessage(webResourcePayload);
    return publisher.send(message);
  }

  protected WebResourcePayload getWebResourcePayload(String workspace, String filePath)
      throws GitHubActionException {
    return new WebResourcePayload(workspace, filePath);
  }

  private String getChannel() {
    return WEB_RESOURCES_CHANNEL;
  }

  protected JsonNode prepareIngestionMessage(WebResourcePayload payload)
      throws GitHubActionException {
    RawPayload rawPayload = payload.resolve();
    TextNode text = TextNode.valueOf(new String(rawPayload.source(),
        StandardCharsets.UTF_8));

    ObjectNode payloadContent = objectMapper.createObjectNode();
    ObjectNode content = payloadContent.putObject(CONTENT_NODE_NAME);
    content.set(BYTES_CONTENT_NODE_NAME, text);
    payloadContent.set(CONTENT_NODE_NAME, content);

    JsonNode message = IngestionMessageJsonFactory.from(
        payload.getFilePath(),
        Message.PUBLISH_ACTION,
        payloadContent,
        PUBLISH_PROPERTIES,
        WEB_RESOURCE_SCHEMA_TYPE
    );
    if (log.isDebugEnabled()) {
      log.debug(String.format("Payload: %s", message.toPrettyString()));
    }
    return message;
  }

  protected StreamxClientProvider getStreamxClientProvider() {
    return streamxClientProvider;
  }

  protected GitService getGitService() {
    return gitService;
  }
}
