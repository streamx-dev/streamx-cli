package dev.streamx.githhub.provider.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import dev.streamx.clients.ingestion.publisher.Message;
import dev.streamx.exception.GitHubActionException;
import dev.streamx.exception.MissingRequiredInputException;
import dev.streamx.githhub.Constants;
import dev.streamx.githhub.provider.AbstractSourceProviderTest;
import dev.streamx.ingestion.IngestionConfig;
import dev.streamx.ingestion.schema.SchemaProvider;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExternalSourceProviderTest extends AbstractSourceProviderTest {

  private ExternalSourceProvider provider;
  @Mock
  private CloseableHttpClient httpClient;
  @Mock
  private SchemaProvider schemaProvider;
  @Mock
  private IngestionConfig ingestionConfig;
  @Mock
  private CloseableHttpResponse httpResponse;
  @Mock
  private HttpEntity httpEntity;

  @BeforeEach
  public void setUp() throws IOException {
    provider = new ExternalSourceProvider();
    provider.httpClient = httpClient;
    provider.objectMapper = objectMapper;
    provider.schemaProvider = schemaProvider;
    provider.ingestionConfig = ingestionConfig;
    lenient().when(httpClient.execute(any())).thenReturn(httpResponse);
    lenient().when(httpResponse.getEntity()).thenReturn(httpEntity);
    lenient().when(httpEntity.getContent()).thenReturn(
        new ByteArrayInputStream("test response".getBytes()));
  }

  @Test
  public void testShouldReturnProviderName() {
    assertEquals("ExternalSourceProvider", provider.getName());
  }

  @Test
  public void testShouldThrowAnExceptionWhenStreamxIngestionUrlParameterMissing() {
    MissingRequiredInputException exception = assertThrows(
        MissingRequiredInputException.class, () ->
            provider.createPayload(inputs, context, payload)
    );
    assertEquals("Provider is missing required input parameter: streamx-ingestion-url",
        exception.getMessage());
  }

  @Test
  public void testShouldThrowAnExceptionWhenChannelParameterMissing() {
    when(inputs.get(Constants.STREAMX_INGESTION_URL)).thenReturn(
        Optional.of("https://ingestion.streamx.dev"));

    MissingRequiredInputException exception = assertThrows(
        MissingRequiredInputException.class, () ->
            provider.createPayload(inputs, context, payload)
    );
    assertEquals("Provider is missing required input parameter: channel",
        exception.getMessage());
  }

  @Test
  public void testShouldThrowAnExceptionWhenExternalResourceUrlParameterMissing() {
    when(inputs.get(Constants.STREAMX_INGESTION_URL)).thenReturn(
        Optional.of("https://ingestion.streamx.dev"));
    when(inputs.get(Constants.INGESTION_CHANNEL)).thenReturn(
        Optional.of("pages"));

    MissingRequiredInputException exception = assertThrows(
        MissingRequiredInputException.class, () ->
            provider.createPayload(inputs, context, payload)
    );
    assertEquals("Provider is missing required input parameter: external-resource-url",
        exception.getMessage());
  }

  @Test
  public void testShouldThrowAnExceptionWhenKeyParameterMissing() {
    when(inputs.get(Constants.STREAMX_INGESTION_URL)).thenReturn(
        Optional.of("https://ingestion.streamx.dev"));
    when(inputs.get(Constants.INGESTION_CHANNEL)).thenReturn(
        Optional.of("pages"));
    when(inputs.get(Constants.EXTERNAL_RESOURCE_URL)).thenReturn(
        Optional.of("https://test.dev/my/resource"));

    MissingRequiredInputException exception = assertThrows(
        MissingRequiredInputException.class, () ->
            provider.createPayload(inputs, context, payload)
    );
    assertEquals("Provider is missing required input parameter: key",
        exception.getMessage());
  }

  @Test
  public void testShouldRequestSchemaTypeForChannel()
      throws GitHubActionException {
    when(inputs.get(Constants.STREAMX_INGESTION_URL)).thenReturn(
        Optional.of("https://ingestion.streamx.dev"));
    when(inputs.get(Constants.STREAMX_INGESTION_TOKEN)).thenReturn(
        Optional.of("ingestion_token"));
    when(inputs.get(Constants.INGESTION_CHANNEL)).thenReturn(
        Optional.of("pages"));

    when(inputs.get(Constants.INGESTION_ACTION)).thenReturn(
        Optional.of(Message.PUBLISH_ACTION));
    when(inputs.get(Constants.EXTERNAL_RESOURCE_URL)).thenReturn(
        Optional.of("https://test.dev/my/resource"));
    when(inputs.get(Constants.INGESTION_MESSAGE_KEY)).thenReturn(
        Optional.of("my/resource/key"));
    when(inputs.get(Constants.INGESTION_CHANNEL)).thenReturn(
        Optional.of("pages"));
    when(schemaProvider.getSchemaType("https://ingestion.streamx.dev",
        "ingestion_token", "pages"))
        .thenReturn("dev.streamx.blueprints.data.Page");

    List<JsonNode> result = provider.createPayload(inputs, context, payload);
    assertNotNull(result);
    assertEquals(1, result.size());
    assertTrue(result.get(0).get("payload").has("dev.streamx.blueprints.data.Page"));
  }

  @Test
  public void testShouldMakeIngestionMessageWithSxTypeParam()
      throws GitHubActionException {
    when(inputs.get(Constants.STREAMX_INGESTION_URL)).thenReturn(
        Optional.of("https://ingestion.streamx.dev"));
    when(inputs.get(Constants.STREAMX_INGESTION_TOKEN)).thenReturn(
        Optional.of("ingestion_token"));
    when(inputs.get(Constants.INGESTION_CHANNEL)).thenReturn(
        Optional.of("pages"));

    when(inputs.get(Constants.INGESTION_ACTION)).thenReturn(
        Optional.of(Message.PUBLISH_ACTION));
    when(inputs.get(Constants.EXTERNAL_RESOURCE_URL)).thenReturn(
        Optional.of("https://test.dev/my/resource"));
    when(inputs.get(Constants.INGESTION_MESSAGE_KEY)).thenReturn(
        Optional.of("my/resource/key"));
    when(inputs.get(Constants.INGESTION_CHANNEL)).thenReturn(
        Optional.of("pages"));
    when(schemaProvider.getSchemaType("https://ingestion.streamx.dev",
        "ingestion_token", "pages"))
        .thenReturn("dev.streamx.blueprints.data.Page");

    when(inputs.get(Constants.INGESTION_TYPE)).thenReturn(Optional.of("page/eds"));

    List<JsonNode> result = provider.createPayload(inputs, context, payload);
    assertNotNull(result);
    assertEquals(1, result.size());
    assertTrue(result.get(0).has("properties"));
    JsonNode propertiesNode = result.get(0).get("properties");
    assertEquals("page/eds", propertiesNode.get("sx:type").asText());
  }


}