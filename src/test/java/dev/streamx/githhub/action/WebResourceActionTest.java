package dev.streamx.githhub.action;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.streamx.clients.ingestion.StreamxClient;
import dev.streamx.clients.ingestion.exceptions.StreamxClientException;
import dev.streamx.clients.ingestion.publisher.Publisher;
import dev.streamx.clients.ingestion.publisher.SuccessResult;
import dev.streamx.exception.GitHubActionException;
import dev.streamx.githhub.git.GitService;
import dev.streamx.githhub.git.impl.DiffResult;
import dev.streamx.ingestion.StreamxClientProvider;
import dev.streamx.ingestion.payload.RawPayload;
import dev.streamx.ingestion.payload.WebResourcePayload;
import io.quarkiverse.githubaction.Commands;
import io.quarkiverse.githubaction.Context;
import io.quarkiverse.githubaction.Inputs;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kohsuke.github.GHEventPayload;
import org.kohsuke.github.GHEventPayload.PullRequest;
import org.kohsuke.github.GHPullRequest;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WebResourceActionTest {

  private ObjectMapper objectMapper = new ObjectMapper();

  @Mock
  private StreamxClientProvider streamxClientProvider;
  @Mock
  private GitService gitService;
  @Mock
  private Context context;
  @Mock
  private Commands commands;
  @Mock
  private Inputs inputs;
  @Mock
  private StreamxClient streamxClient;
  @Mock
  private Publisher<JsonNode> publisher;

  private WebResourceAction instance;

  @BeforeEach
  public void setUp() throws StreamxClientException {
    instance = new WebResourceAction(streamxClientProvider, gitService, objectMapper) {
      @Override
      protected WebResourcePayload getWebResourcePayload(String workspace, String filePath)
          throws GitHubActionException {
        WebResourcePayload payload = mock(WebResourcePayload.class);
        when(payload.getFilePath()).thenReturn(filePath);
        when(payload.resolve()).thenReturn(
            new RawPayload(("Content of " + filePath + " in workspace " + workspace).getBytes()));
        return payload;
      }
    };
    lenient().when(streamxClient.newPublisher(anyString(), eq(JsonNode.class)))
        .thenReturn(publisher);
  }

  @Test
  public void testShouldRejectInjectionWhenMissingWebresourceIncludeVariable() throws IOException {
    PullRequest pullRequestPayload = mockPullRequestPayload(2);

    instance.webresourceSyncOnPullRequestMerged(commands, inputs, pullRequestPayload, context);

    verify(commands, times(1)).error(
        startsWith("Missing required STREAMX_INGESTION_WEBRESOURCE_INCLUDES variable"));
  }

  @Test
  public void testShouldRejectInjectionWhenMissingStreamxIngestionVariable() throws IOException {
    PullRequest pullRequestPayload = mockPullRequestPayload(2);
    when(inputs.get(WebResourceAction.STREAMX_INGESTION_WEBRESOURCE_INCLUDES)).thenReturn(
        Optional.of("[]"));

    instance.webresourceSyncOnPullRequestMerged(commands, inputs, pullRequestPayload, context);

    verify(commands, times(1)).error(
        startsWith("Missing required STREAMX_INGESTION_BASE_URL variable"));
  }

  @Test
  public void testShouldPublishFilesToStreamX()
      throws IOException, StreamxClientException, GitHubActionException {
    when(context.getGitHubWorkspace()).thenReturn("/var/workspace");
    when(inputs.get(WebResourceAction.STREAMX_INGESTION_URL)).thenReturn(
        Optional.of("https://ingestion.localhost.com"));
    Optional<String> secretToken = Optional.of("secret_token");
    when(inputs.get(WebResourceAction.STREAMX_INGESTION_TOKEN)).thenReturn(secretToken);
    when(inputs.get(WebResourceAction.STREAMX_INGESTION_WEBRESOURCE_INCLUDES)).thenReturn(
        Optional.of("[\"styles/*.css\"]"));

    when(streamxClientProvider.createStreamxClient("https://ingestion.localhost.com",
        secretToken)).thenReturn(streamxClient);

    DiffResult diffResult = createMockDiffResult(
        Set.of("styles/main.css", "scripts/script.js"),
        Collections.emptySet());
    when(gitService.getDiff("/var/workspace", 2)).thenReturn(diffResult);
    when(publisher.send(any(JsonNode.class))).thenReturn(
        new SuccessResult(System.currentTimeMillis(), "styles/main.css"));

    instance.webresourceSyncOnPullRequestMerged(commands, inputs,
        mockPullRequestPayload(2), context);

    verify(publisher, times(1)).send(argThat((JsonNode jsonNode) -> {
      assertEquals("styles/main.css", jsonNode.get("key").textValue());
      assertEquals("publish", jsonNode.get("action").textValue());
      assertTrue(jsonNode.toPrettyString()
          .contains("Content of styles/main.css in workspace /var/workspace"));
      return true;
    }));
  }

  @Test
  public void testShouldUnpublishFilesFromStreamX()
      throws IOException, StreamxClientException, GitHubActionException {
    when(context.getGitHubWorkspace()).thenReturn("/var/workspace");
    when(inputs.get(WebResourceAction.STREAMX_INGESTION_URL)).thenReturn(
        Optional.of("https://ingestion.localhost.com"));
    Optional<String> secretToken = Optional.of("secret_token");
    when(inputs.get(WebResourceAction.STREAMX_INGESTION_TOKEN)).thenReturn(secretToken);
    when(inputs.get(WebResourceAction.STREAMX_INGESTION_WEBRESOURCE_INCLUDES)).thenReturn(
        Optional.of("[\"styles/*.css\"]"));

    when(streamxClientProvider.createStreamxClient("https://ingestion.localhost.com",
        secretToken)).thenReturn(streamxClient);

    DiffResult diffResult = createMockDiffResult(
        Collections.emptySet(),
        Set.of("styles/main.css", "scripts/script.js")
    );
    when(gitService.getDiff("/var/workspace", 2)).thenReturn(diffResult);
    when(publisher.unpublish("styles/main.css")).thenReturn(
        new SuccessResult(System.currentTimeMillis(), "styles/main.css"));

    instance.webresourceSyncOnPullRequestMerged(commands, inputs,
        mockPullRequestPayload(2), context);

    verify(publisher, times(1)).unpublish("styles/main.css");
  }


  @Test
  void prepareIngestionMessage() throws GitHubActionException {
    RawPayload rawPayload = new RawPayload("test content".getBytes(StandardCharsets.UTF_8));
    WebResourcePayload payload = mock(WebResourcePayload.class);
    when(payload.resolve()).thenReturn(rawPayload);
    when(payload.getFilePath()).thenReturn("styles/main.css");

    JsonNode result = instance.prepareIngestionMessage(payload);
    assertNotNull(result);
    assertEquals("""
        {
          "key" : "styles/main.css",
          "action" : "publish",
          "eventTime" : null,
          "properties" : {
            "sx:type" : "web-resource/static"
          },
          "payload" : {
            "dev.streamx.blueprints.data.WebResource" : {
              "content" : {
                "bytes" : "test content"
              }
            }
          }
        }""", result.toPrettyString());
  }

  private GHEventPayload.PullRequest mockPullRequestPayload(int numberOfCommits)
      throws IOException {
    GHEventPayload.PullRequest pullRequest = mock(GHEventPayload.PullRequest.class);
    GHPullRequest ghPullRequest = mock(GHPullRequest.class);
    lenient().when(pullRequest.getPullRequest()).thenReturn(ghPullRequest);
    lenient().when(ghPullRequest.getCommits()).thenReturn(numberOfCommits);
    return pullRequest;
  }

  private DiffResult createMockDiffResult(Set<String> updates, Set<String> deletions) {
    DiffResult diffResult = mock(DiffResult.class);
    when(diffResult.getModifiedPaths()).thenReturn(updates);
    when(diffResult.getDeletedPaths()).thenReturn(deletions);
    return diffResult;
  }

}