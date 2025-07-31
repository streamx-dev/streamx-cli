package dev.streamx.githhub.provider.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import dev.streamx.exception.GitHubActionException;
import dev.streamx.exception.MissingRequiredInputException;
import dev.streamx.githhub.Constants;
import dev.streamx.githhub.provider.AbstractSourceProviderTest;
import dev.streamx.ingestion.schema.SchemaProvider;
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

  private BatchSourceProvider provider;
  @Mock
  private Logger log;
  @Mock
  private SchemaProvider schemaProvider;

  @BeforeEach
  public void setUp() {
    provider = new BatchSourceProvider();
    provider.log = log;
    provider.objectMapper = objectMapper;
    provider.schemaProvider = schemaProvider;
  }

  @Test
  public void testShouldReturnProviderName() {
    assertEquals("BatchSourceProvider", provider.getName());
  }

  @Test
  public void testShouldThrowAnExceptionWhenIncludePatternsParameterMissing() {
    mockIngestionSchemaInputParameters();
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
    mockIngestionSchemaInputParameters();
    Path testWorkspacePath = getTestWorkspacePath();
    when(inputs.get(Constants.INGESTION_TYPE)).thenReturn(Optional.empty());
    when(inputs.get(Constants.INGESTION_WORKSPACE))
        .thenReturn(Optional.of(testWorkspacePath.toAbsolutePath().toString()));
    when(inputs.get(Constants.INGESTION_INCLUDE_PATTERNS))
        .thenReturn(Optional.of("[\"**/*.js\"]"));
    when(schemaProvider.getSchemaType("https://ingestion.streamx.dev",
        "ingestion_token", "pages"))
        .thenReturn("dev.streamx.blueprints.data.WebResource");

    List<JsonNode> result = provider.createPayload(inputs, context, payload);

    assertFalse(result.isEmpty());
    result.forEach(node -> {
      assertTrue(StringUtils.endsWith(node.get("key").asText(), ".js"));
    });
  }

  @Test
  public void testShouldFindMatchingScriptFilesForFallbackWorkspace() throws GitHubActionException {
    mockIngestionSchemaInputParameters();
    Path testWorkspacePath = getTestWorkspacePath();
    when(inputs.get(Constants.INGESTION_TYPE)).thenReturn(Optional.empty());
    when(inputs.get(Constants.INGESTION_WORKSPACE)).thenReturn(null);
    when(context.getGitHubWorkspace()).thenReturn(testWorkspacePath.toAbsolutePath().toString());
    when(inputs.get(Constants.INGESTION_INCLUDE_PATTERNS))
        .thenReturn(Optional.of("[\"**/*.js\"]"));
    when(schemaProvider.getSchemaType("https://ingestion.streamx.dev",
        "ingestion_token", "pages"))
        .thenReturn("dev.streamx.blueprints.data.WebResource");

    List<JsonNode> result = provider.createPayload(inputs, context, payload);

    assertFalse(result.isEmpty());
    result.forEach(node -> {
      assertTrue(StringUtils.endsWith(node.get("key").asText(), ".js"));
    });
  }

  private void mockIngestionSchemaInputParameters() {
    lenient().when(inputs.get(Constants.STREAMX_INGESTION_URL)).thenReturn(
        Optional.of("https://ingestion.streamx.dev"));
    lenient().when(inputs.get(Constants.STREAMX_INGESTION_TOKEN)).thenReturn(
        Optional.of("ingestion_token"));
    lenient().when(inputs.get(Constants.INGESTION_CHANNEL)).thenReturn(
        Optional.of("pages"));
  }

}
