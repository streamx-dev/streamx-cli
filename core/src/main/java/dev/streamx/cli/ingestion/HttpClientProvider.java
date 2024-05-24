package dev.streamx.cli.ingestion;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Dependent
public class HttpClientProvider {

  @ConfigProperty(name = "validation.ssl.enabled", defaultValue = "true")
  boolean sslEnabled;

  @ApplicationScoped
  CloseableHttpClient ingestionHttpClient()
      throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {

    HttpClientBuilder builder = HttpClients
        .custom();
    if (!sslEnabled) {
      builder
          .setSSLContext(
              new SSLContextBuilder().loadTrustMaterial(null, TrustAllStrategy.INSTANCE).build())
          .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE);
    }
    return builder.build();
  }
}
