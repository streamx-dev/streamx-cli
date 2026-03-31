package dev.streamx.githhub.provider.git;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import dev.streamx.exception.GitHubActionException;
import dev.streamx.exception.MissingRequiredInputException;
import dev.streamx.githhub.Constants;
import dev.streamx.githhub.provider.AbstractSourceProviderTest;
import io.cloudevents.CloudEvent;
import io.quarkiverse.githubaction.Context;
import io.quarkiverse.githubaction.Inputs;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
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
class PullRequestDiffSourceProviderTest extends AbstractSourceProviderTest {

  private static final String PUBLISH_EVENT_TYPE =
      "com.streamx.blueprints.web-resource.published.v1";
  private static final String UNPUBLISH_EVENT_TYPE =
      "com.streamx.blueprints.web-resource.unpublished.v1";
  private static final DiffResult EMPTY_DIFF_RESULT = new DiffResult();
  private PullRequestDiffDataSourceProvider provider;

  @Mock
  private GitService gitService;
  @Mock
  private GHEventPayload.PullRequest pullRequestPayload;
  @Mock
  private GHPullRequest ghPullRequest;

  @BeforeEach
  public void setUp() {
    provider = new PullRequestDiffDataSourceProvider();
    provider.objectMapper = objectMapper;
    provider.gitService = gitService;

    lenient().when(pullRequestPayload.getPullRequest()).thenReturn(ghPullRequest);
  }

  @Test
  public void testShouldReturnProviderName() {
    assertEquals("PullRequestDiffSourceProvider", provider.getName());
  }

  @Test
  public void testShouldThrowAnExceptionWhenPayloadIsNotPullRequest() {
    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class, () ->
            provider.createPayload(inputs, context, payload)
    );
    assertEquals("Expected PullRequest eventPayload instance", exception.getMessage());
  }

  @Test
  public void testShouldVerifyRequiredInputParameters() throws GitHubActionException, IOException {
    MissingRequiredInputException exception = requestCreatePayloadAndGetException(inputs, context,
        pullRequestPayload);
    assertEquals("Provider is missing required input parameter: streamx-ingestion-url",
        exception.getMessage());

    reset(inputs);
    when(inputs.get(Constants.STREAMX_INGESTION_URL)).thenReturn(
        Optional.of("https://ingestion.streamx.dev"));
    exception = requestCreatePayloadAndGetException(inputs, context,
        pullRequestPayload);
    assertEquals("Provider is missing required input parameter: publish-event-type",
        exception.getMessage());

    reset(inputs);
    when(inputs.get(Constants.STREAMX_INGESTION_URL)).thenReturn(
        Optional.of("https://ingestion.streamx.dev"));
    when(inputs.get(Constants.PUBLISH_EVENT_TYPE)).thenReturn(
        Optional.of(PUBLISH_EVENT_TYPE));
    exception = requestCreatePayloadAndGetException(inputs, context,
        pullRequestPayload);
    assertEquals("Provider is missing required input parameter: unpublish-event-type",
        exception.getMessage());

    reset(inputs);
    when(inputs.get(Constants.STREAMX_INGESTION_URL)).thenReturn(
        Optional.of("https://ingestion.streamx.dev"));
    when(inputs.get(Constants.PUBLISH_EVENT_TYPE)).thenReturn(
        Optional.of(PUBLISH_EVENT_TYPE));
    when(inputs.get(Constants.UNPUBLISH_EVENT_TYPE)).thenReturn(
        Optional.of(UNPUBLISH_EVENT_TYPE));
    exception = requestCreatePayloadAndGetException(inputs, context,
        pullRequestPayload);
    assertEquals("Provider is missing required input parameter: include-patterns",
        exception.getMessage());

    reset(inputs);
    when(inputs.get(Constants.STREAMX_INGESTION_URL)).thenReturn(
        Optional.of("https://ingestion.streamx.dev"));
    when(inputs.get(Constants.PUBLISH_EVENT_TYPE)).thenReturn(
        Optional.of(PUBLISH_EVENT_TYPE));
    when(inputs.get(Constants.UNPUBLISH_EVENT_TYPE)).thenReturn(
        Optional.of(UNPUBLISH_EVENT_TYPE));
    when(inputs.get(Constants.INGESTION_INCLUDE_PATTERNS)).thenReturn(
        Optional.of("[\"styles/*.css\"]"));
    when(inputs.get(Constants.INGESTION_WORKSPACE)).thenReturn(
        Optional.of("/opt/streamx/ingestion"));
    when(ghPullRequest.getCommits()).thenReturn(1);
    when(gitService.getDiff(eq("/opt/streamx/ingestion"), eq(1))).thenReturn(EMPTY_DIFF_RESULT);
    List<CloudEvent> result = provider.createPayload(inputs, context, pullRequestPayload);
    assertTrue(result.isEmpty());
  }

  private MissingRequiredInputException requestCreatePayloadAndGetException(Inputs inputs,
      Context context, PullRequest pullRequestPayload) {
    MissingRequiredInputException exception = assertThrows(
        MissingRequiredInputException.class, () ->
            provider.createPayload(inputs, context, pullRequestPayload)
    );
    return exception;
  }

  @Test
  public void testShouldCreatePublishIngestionMessages() throws GitHubActionException, IOException {
    Path testWorkspacePath = getTestWorkspacePath();
    String testWorkspace = testWorkspacePath.toAbsolutePath().toString();

    when(inputs.get(Constants.STREAMX_INGESTION_URL)).thenReturn(
        Optional.of("https://ingestion.streamx.dev"));
    when(inputs.get(Constants.PUBLISH_EVENT_TYPE)).thenReturn(
        Optional.of(PUBLISH_EVENT_TYPE));
    when(inputs.get(Constants.UNPUBLISH_EVENT_TYPE)).thenReturn(
        Optional.of(UNPUBLISH_EVENT_TYPE));
    when(inputs.get(Constants.INGESTION_WORKSPACE)).thenReturn(Optional.of(testWorkspace));
    when(inputs.get(Constants.INGESTION_INCLUDE_PATTERNS)).thenReturn(
        Optional.of("[\"**.css\"]"));
    when(ghPullRequest.getCommits()).thenReturn(1);
    DiffResult diffResult = mock(DiffResult.class);
    when(gitService.getDiff(eq(testWorkspace), eq(1))).thenReturn(diffResult);
    when(diffResult.getModifiedPaths()).thenReturn(
        Set.of("test_1/file_1_1.css", "test_1/file_1_2.css"));

    List<CloudEvent> result = provider.createPayload(inputs, context, pullRequestPayload);
    assertFalse(result.isEmpty());
    result.forEach(event -> assertEquals(PUBLISH_EVENT_TYPE, event.getType()));
  }

  @Test
  public void testShouldCreateUnpublishIngestionMessages()
      throws GitHubActionException, IOException {
    Path testWorkspacePath = getTestWorkspacePath();
    String testWorkspace = testWorkspacePath.toAbsolutePath().toString();

    when(inputs.get(Constants.STREAMX_INGESTION_URL)).thenReturn(
        Optional.of("https://ingestion.streamx.dev"));
    when(inputs.get(Constants.PUBLISH_EVENT_TYPE)).thenReturn(
        Optional.of(PUBLISH_EVENT_TYPE));
    when(inputs.get(Constants.UNPUBLISH_EVENT_TYPE)).thenReturn(
        Optional.of(UNPUBLISH_EVENT_TYPE));
    when(inputs.get(Constants.INGESTION_WORKSPACE)).thenReturn(Optional.of(testWorkspace));
    when(inputs.get(Constants.INGESTION_INCLUDE_PATTERNS)).thenReturn(
        Optional.of("[\"**.css\", \"**.js\"]"));
    when(ghPullRequest.getCommits()).thenReturn(1);
    DiffResult diffResult = mock(DiffResult.class);
    when(gitService.getDiff(eq(testWorkspace), eq(1))).thenReturn(diffResult);
    when(diffResult.getDeletedPaths()).thenReturn(
        Set.of("test_2/file_2_1.css", "test_2/file_2_1.js"));

    List<CloudEvent> result = provider.createPayload(inputs, context, pullRequestPayload);
    assertFalse(result.isEmpty());
    result.forEach(event -> {
      assertTrue(event.getSubject().equals("test_2/file_2_1.css")
          || event.getSubject().equals("test_2/file_2_1.js"));
      assertEquals(UNPUBLISH_EVENT_TYPE, event.getType());
      assertTrue(event.getData() == null);
    });
  }

}
