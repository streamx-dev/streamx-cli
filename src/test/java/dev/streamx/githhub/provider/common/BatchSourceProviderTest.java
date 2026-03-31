package dev.streamx.githhub.provider.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import dev.streamx.exception.GitHubActionException;
import dev.streamx.exception.MissingRequiredInputException;
import dev.streamx.githhub.Constants;
import dev.streamx.githhub.provider.AbstractSourceProviderTest;
import io.cloudevents.CloudEvent;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class BatchSourceProviderTest extends AbstractSourceProviderTest {

  private static final String PUBLISH_EVENT_TYPE =
      "com.streamx.blueprints.web-resource.published.v1";

  private BatchSourceProvider provider;
  @Mock
  private Logger log;

  @BeforeEach
  public void setUp() {
    provider = new BatchSourceProvider();
    provider.log = log;
    provider.objectMapper = objectMapper;
  }

  @Test
  public void testShouldReturnProviderName() {
    assertEquals("BatchSourceProvider", provider.getName());
  }

  @Test
  public void testShouldThrowAnExceptionWhenIncludePatternsParameterMissing() {
    mockIngestionInputParameters();
    when(inputs.get(Constants.INGESTION_INCLUDE_PATTERNS))
        .thenReturn(Optional.empty());
    MissingRequiredInputException exception = assertThrows(
        MissingRequiredInputException.class, () ->
            provider.createPayload(inputs, context, payload)
    );
    assertEquals("Provider is missing required input parameter: include-patterns",
        exception.getMessage());
  }

  @Test
  public void testShouldFindMatchingScriptFilesForGivenWorkspace() throws GitHubActionException {
    mockIngestionInputParameters();
    Path testWorkspacePath = getTestWorkspacePath();
    when(inputs.get(Constants.INGESTION_WORKSPACE))
        .thenReturn(Optional.of(testWorkspacePath.toAbsolutePath().toString()));
    when(inputs.get(Constants.INGESTION_INCLUDE_PATTERNS))
        .thenReturn(Optional.of("[\"**/*.js\"]"));

    List<CloudEvent> result = provider.createPayload(inputs, context, payload);

    assertFalse(result.isEmpty());
    result.forEach(event -> {
      assertFalse(StringUtils.isBlank(event.getSubject()));
      assertFalse(StringUtils.isBlank(event.getType()));
      assertEquals(PUBLISH_EVENT_TYPE, event.getType());
    });
  }

  @Test
  public void testShouldFindMatchingScriptFilesForFallbackWorkspace()
      throws GitHubActionException {
    mockIngestionInputParameters();
    Path testWorkspacePath = getTestWorkspacePath();
    when(inputs.get(Constants.INGESTION_WORKSPACE)).thenReturn(null);
    when(context.getGitHubWorkspace()).thenReturn(testWorkspacePath.toAbsolutePath().toString());
    when(inputs.get(Constants.INGESTION_INCLUDE_PATTERNS))
        .thenReturn(Optional.of("[\"**/*.js\"]"));

    List<CloudEvent> result = provider.createPayload(inputs, context, payload);

    assertFalse(result.isEmpty());
    result.forEach(event -> {
      assertFalse(StringUtils.isBlank(event.getSubject()));
      assertEquals(PUBLISH_EVENT_TYPE, event.getType());
    });
  }

  private void mockIngestionInputParameters() {
    lenient().when(inputs.get(Constants.STREAMX_INGESTION_URL)).thenReturn(
        Optional.of("https://ingestion.streamx.dev"));
    lenient().when(inputs.get(Constants.PUBLISH_EVENT_TYPE)).thenReturn(
        Optional.of(PUBLISH_EVENT_TYPE));
  }

}
