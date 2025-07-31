package dev.streamx.githhub.action;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;

import com.fasterxml.jackson.databind.JsonNode;
import dev.streamx.clients.ingestion.StreamxClient;
import dev.streamx.clients.ingestion.exceptions.StreamxClientException;
import dev.streamx.clients.ingestion.publisher.Publisher;
import dev.streamx.githhub.Constants;
import dev.streamx.ingestion.StreamxClientProvider;
import io.quarkiverse.githubaction.Commands;
import io.quarkiverse.githubaction.Inputs;
import java.util.Optional;
import org.junit.jupiter.api.Disabled;
import org.mockito.Mock;

@Disabled
abstract class AbstractGitHubActionTest {

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

  public void setUp() throws StreamxClientException {
    lenient().when(streamxClientProvider.createStreamxClient(any(), any()))
        .thenReturn(streamxClient);
    lenient().when(streamxClient.newPublisher(any(), eq(JsonNode.class))).thenReturn(publisher);
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


}
