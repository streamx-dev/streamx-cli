package dev.streamx.cli.ingestion;

import dev.streamx.clients.ingestion.StreamxClient;
import dev.streamx.clients.ingestion.StreamxClientBuilder;
import dev.streamx.clients.ingestion.exceptions.StreamxClientException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.impl.client.CloseableHttpClient;

@ApplicationScoped
public class StreamxClientProvider {

  @Inject
  IngestionClientContext ingestionClientContext;

  @Inject
  CloseableHttpClient httpClient;

  public StreamxClient createStreamxClient() throws StreamxClientException {
    StreamxClientBuilder builder = StreamxClient.builder(ingestionClientContext.getIngestionUrl())
        .setApacheHttpClient(httpClient);

    if (StringUtils.isNotEmpty(ingestionClientContext.getOauth2Bearer())) {
      builder.setAuthToken(ingestionClientContext.getOauth2Bearer());
    }

    return builder.build();
  }
}
