package dev.streamx.cli.publish;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

@Dependent
public class HttpClientProvider {

  @ApplicationScoped
  CloseableHttpClient ingestionHttpClient() {
    return HttpClients.createDefault();
  }
}
