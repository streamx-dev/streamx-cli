package dev.streamx.githhub.action;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.TokenBuffer;
import dev.streamx.clients.ingestion.StreamxClient;
import dev.streamx.clients.ingestion.exceptions.StreamxClientException;
import dev.streamx.clients.ingestion.publisher.Publisher;
import dev.streamx.githhub.Constants;
import dev.streamx.ingestion.IngestionConfig;
import dev.streamx.ingestion.StreamxClientProvider;
import io.quarkiverse.githubaction.Commands;
import io.quarkiverse.githubaction.Inputs;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.junit.jupiter.api.Disabled;
import org.mockito.Mock;

@Disabled
abstract class AbstractGitHubActionTest {

  protected ObjectMapper objectMapper = new ObjectMapper();
  @Mock
  protected Commands commands;
  @Mock
  protected Inputs inputs;
  @Mock
  protected StreamxClientProvider streamxClientProvider;
  @Mock
  protected StreamxClient streamxClient;
  @Mock
  protected Publisher<JsonNode> publisher;
  @Mock
  protected IngestionConfig ingestionConfig;

  public void setUp() throws StreamxClientException {
    lenient().when(streamxClientProvider.createStreamxClient(any(), any()))
        .thenReturn(streamxClient);
    lenient().when(streamxClient.newPublisher(any(), eq(JsonNode.class))).thenReturn(publisher);
    lenient().when(ingestionConfig.batchSourceProviderBatchSizeInBytes()).thenReturn(3000000L);
  }

  protected void mockBaseInputParameters() {
    String streamxUrl = "https://ingestion.streamx.dev";
    lenient().when(inputs.getRequired(Constants.STREAMX_INGESTION_URL)).thenReturn(streamxUrl);
    lenient().when(inputs.get(Constants.STREAMX_INGESTION_URL)).thenReturn(Optional.of(streamxUrl));

    String streamxToken = "streamx_token";
    lenient().when(inputs.getRequired(Constants.STREAMX_INGESTION_TOKEN)).thenReturn(streamxToken);
    lenient().when(inputs.get(Constants.STREAMX_INGESTION_TOKEN))
        .thenReturn(Optional.of(streamxToken));
  }

  protected JsonNode createTestPayloadContent(String content) {
    ObjectMapper objectMapper = new ObjectMapper();
    try (var generator = new TokenBuffer(objectMapper, false)) {
      generator.writeString(new String(content.getBytes(), StandardCharsets.ISO_8859_1));
      return objectMapper.readTree(generator.asParser());
    } catch (IOException exc) {
      return null;
    }
  }

}
