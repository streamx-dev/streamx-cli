package dev.streamx.ingestion;

import dev.streamx.clients.ingestion.StreamxClient;
import dev.streamx.clients.ingestion.StreamxClientBuilder;
import dev.streamx.clients.ingestion.exceptions.StreamxClientException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Optional;
import org.apache.http.impl.client.CloseableHttpClient;

@ApplicationScoped
public class StreamxClientProvider {

  @Inject
  CloseableHttpClient httpClient;

  public StreamxClient createStreamxClient(String ingestionServiceUrl,
      Optional<String> authToken)
      throws StreamxClientException {
    StreamxClientBuilder builder = StreamxClient.builder(ingestionServiceUrl)
        .setApacheHttpClient(httpClient);
    authToken.ifPresent(builder::setAuthToken);
    return builder.build();
  }

}
