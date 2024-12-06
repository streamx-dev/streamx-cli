package dev.streamx.cli.command.ingestion;

import dev.streamx.clients.ingestion.StreamxClient;
import dev.streamx.clients.ingestion.StreamxClientBuilder;
import dev.streamx.clients.ingestion.exceptions.StreamxClientException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.http.impl.client.CloseableHttpClient;

@ApplicationScoped
public class StreamxClientProvider {

  @Inject
  CloseableHttpClient httpClient;

  public StreamxClient createStreamxClient(IngestionClientConfig ingestionClientConfig)
      throws StreamxClientException {
    StreamxClientBuilder builder = StreamxClient.builder(ingestionClientConfig.url())
        .setApacheHttpClient(httpClient);

    ingestionClientConfig.authToken()
        .ifPresent(builder::setAuthToken);

    return builder.build();
  }
}
