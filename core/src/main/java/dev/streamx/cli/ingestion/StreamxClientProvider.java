package dev.streamx.cli.ingestion;

import dev.streamx.clients.ingestion.StreamxClient;
import dev.streamx.clients.ingestion.exceptions.StreamxClientException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.http.impl.client.CloseableHttpClient;

@ApplicationScoped
public class StreamxClientProvider {

  @Inject
  IngestionClientConfig ingestionClientConfig;

  @Inject
  CloseableHttpClient httpClient;

  public StreamxClient createStreamxClient() throws StreamxClientException {
    return StreamxClient.builder(ingestionClientConfig.ingestionUrl())
        .setApacheHttpClient(httpClient)
        .build();
  }
}
