package dev.streamx.githhub.provider.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import dev.streamx.exception.GitHubActionException;
import dev.streamx.exception.MissingRequiredInputException;
import dev.streamx.githhub.Constants;
import dev.streamx.githhub.provider.AbstractSourceProviderTest;
import dev.streamx.ingestion.IngestionConfig;
import io.cloudevents.CloudEvent;
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

  private static final String PUBLISH_EVENT_TYPE =
      "com.streamx.blueprints.web-resource.published.v1";

  private ExternalSourceProvider provider;
  @Mock
  private CloseableHttpClient httpClient;
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
  public void testShouldThrowAnExceptionWhenEventTypeParameterMissing() {
    when(inputs.get(Constants.STREAMX_INGESTION_URL)).thenReturn(
        Optional.of("https://ingestion.streamx.dev"));

    MissingRequiredInputException exception = assertThrows(
        MissingRequiredInputException.class, () ->
            provider.createPayload(inputs, context, payload)
    );
    assertEquals("Provider is missing required input parameter: publish-event-type",
        exception.getMessage());
  }

  @Test
  public void testShouldThrowAnExceptionWhenExternalResourceUrlParameterMissing() {
    when(inputs.get(Constants.STREAMX_INGESTION_URL)).thenReturn(
        Optional.of("https://ingestion.streamx.dev"));
    when(inputs.get(Constants.PUBLISH_EVENT_TYPE)).thenReturn(
        Optional.of(PUBLISH_EVENT_TYPE));

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
    when(inputs.get(Constants.PUBLISH_EVENT_TYPE)).thenReturn(
        Optional.of(PUBLISH_EVENT_TYPE));
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
  public void testShouldCreateCloudEventWithCorrectType()
      throws GitHubActionException {
    when(inputs.get(Constants.STREAMX_INGESTION_URL)).thenReturn(
        Optional.of("https://ingestion.streamx.dev"));
    when(inputs.get(Constants.PUBLISH_EVENT_TYPE)).thenReturn(
        Optional.of(PUBLISH_EVENT_TYPE));
    when(inputs.get(Constants.EXTERNAL_RESOURCE_URL)).thenReturn(
        Optional.of("https://test.dev/my/resource"));
    when(inputs.get(Constants.INGESTION_MESSAGE_KEY)).thenReturn(
        Optional.of("my/resource/key"));

    List<CloudEvent> result = provider.createPayload(inputs, context, payload);
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(PUBLISH_EVENT_TYPE, result.get(0).getType());
    assertEquals("my/resource/key", result.get(0).getSubject());
  }

}
