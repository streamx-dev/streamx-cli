package dev.streamx.githhub.action;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;

import com.streamx.clients.ingestion.StreamxClient;
import com.streamx.clients.ingestion.exceptions.StreamxClientException;
import com.streamx.clients.ingestion.publisher.Publisher;
import dev.streamx.githhub.Constants;
import dev.streamx.ingestion.IngestionConfig;
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
  protected Publisher publisher;
  @Mock
  protected IngestionConfig ingestionConfig;

  public void setUp() throws StreamxClientException {
    lenient().when(streamxClientProvider.createStreamxClient(any(), any()))
        .thenReturn(streamxClient);
    lenient().when(streamxClient.newPublisher()).thenReturn(publisher);
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

}
