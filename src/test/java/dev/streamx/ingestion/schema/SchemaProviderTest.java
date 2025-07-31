package dev.streamx.ingestion.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.streamx.exception.GitHubActionException;
import dev.streamx.exception.UnknownChannelException;
import dev.streamx.ingestion.IngestionConfig;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SchemaProviderTest {

  private SchemaProvider schemaProvider;
  @Mock
  private IngestionConfig ingestionConfig;
  @Mock
  private CloseableHttpClient httpClient;
  @Mock
  private CloseableHttpResponse httpResponse;
  @Mock
  private StatusLine statusLine;
  @Mock
  private HttpEntity httpEntity;

  private ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  public void setUp() {
    schemaProvider = new SchemaProvider();
    schemaProvider.ingestionConfig = ingestionConfig;
    schemaProvider.httpClient = httpClient;
    schemaProvider.objectMapper = objectMapper;
    when(httpResponse.getEntity()).thenReturn(httpEntity);
    when(httpResponse.getStatusLine()).thenReturn(statusLine);
  }

  @Test
  public void testShouldThrowExceptionWhenNoSchemaTypeFound()
      throws IOException {
    String channelsApi = "https://ingestion.streamx.dev/ingestion/v1/channels";
    when(ingestionConfig.ingestionChannelsApi()).thenReturn("/ingestion/v1/channels");
    mockDefaultChannelResponse(channelsApi, "{}".getBytes());

    UnknownChannelException exception = assertThrows(UnknownChannelException.class, () ->
        schemaProvider.getSchemaType("https://ingestion.streamx.dev",
            "streamx_token", "web-resources")
    );
    assertEquals("Channel 'web-resources' not found. Available channels are: []",
        exception.getMessage());
  }

  @Test
  public void testShouldThrowExceptionWhenSchemaTypeDoesNotMatch()
      throws IOException {
    String channelsApi = "https://ingestion.streamx.dev/ingestion/v1/channels";
    when(ingestionConfig.ingestionChannelsApi()).thenReturn("/ingestion/v1/channels");
    mockDefaultChannelResponse(channelsApi, readDefaultSchemaResponse());

    UnknownChannelException exception = assertThrows(UnknownChannelException.class, () ->
        schemaProvider.getSchemaType("https://ingestion.streamx.dev",
            "streamx_token", "fake_channel")
    );
    assertEquals("Channel 'fake_channel' not found. Available channels are: "
        + "[pages,assets,data,renderers,rendering-contexts,fragments,compositions,"
        + "web-resources,layouts]", exception.getMessage());
  }

  private void mockDefaultChannelResponse(String channelsApi, byte[] buf) throws IOException {
    when(httpClient.execute(
        argThat((HttpGet get) -> StringUtils.equals(channelsApi, get.getURI().toString()))))
        .thenReturn(httpResponse);
    when(statusLine.getStatusCode()).thenReturn(HttpStatus.SC_OK);
    when(httpEntity.getContent()).thenReturn(new ByteArrayInputStream(buf));
  }

  @Test
  public void testShouldFindMatchingSchemaForGivenWebResourceChannel()
      throws IOException, GitHubActionException {
    String channelsApi = "https://ingestion.streamx.dev/ingestion/v1/channels";
    when(ingestionConfig.ingestionChannelsApi()).thenReturn("/ingestion/v1/channels");
    mockDefaultChannelResponse(channelsApi, readDefaultSchemaResponse());

    String schemaType = schemaProvider.getSchemaType("https://ingestion.streamx.dev",
        "streamx_token", "web-resources");
    assertEquals("dev.streamx.blueprints.data.WebResource", schemaType);
  }

  @Test
  public void testShouldFindMatchingSchemaForGivenPagesChannel()
      throws IOException, GitHubActionException {
    String channelsApi = "https://ingestion.streamx.dev/ingestion/v1/channels";
    when(ingestionConfig.ingestionChannelsApi()).thenReturn("/ingestion/v1/channels");
    mockDefaultChannelResponse(channelsApi, readDefaultSchemaResponse());

    String schemaType = schemaProvider.getSchemaType("https://ingestion.streamx.dev",
        "streamx_token", "pages");
    assertEquals("dev.streamx.blueprints.data.Page", schemaType);
  }


  private byte[] readDefaultSchemaResponse() throws IOException {
    File defaultSchemaResponseFile = new File(
        "src/test/resources/dev/streamx/ingestion/schema/default_schema_response.json");
    return FileUtils.readFileToByteArray(defaultSchemaResponseFile);
  }


}