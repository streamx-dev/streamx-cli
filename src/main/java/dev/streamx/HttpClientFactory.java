package dev.streamx;

import dev.streamx.ingestion.IngestionConfig;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;

@Dependent
public class HttpClientFactory {

  @Inject
  IngestionConfig ingestionConfig;

  @ApplicationScoped
  public CloseableHttpClient httpClient() {
    HttpClientBuilder builder = HttpClients.custom();
    RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();
    requestConfigBuilder.setSocketTimeout(ingestionConfig.connectionSocketTimeoutMillis());
    requestConfigBuilder.setConnectTimeout(ingestionConfig.connectionTimeoutMillis());
    builder.setDefaultRequestConfig(requestConfigBuilder.build());

    return builder.build();
  }
}
