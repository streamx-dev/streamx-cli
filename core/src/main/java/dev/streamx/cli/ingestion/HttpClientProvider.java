package dev.streamx.cli.ingestion;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import javax.net.ssl.SSLContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;

@Dependent
public class HttpClientProvider {

  public static final String HTTPS = "https";
  public static final String HTTP = "http";

  @Inject
  IngestionClientConfig ingestionClientConfig;

  @ApplicationScoped
  CloseableHttpClient ingestionHttpClient()
      throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
    HttpClientBuilder builder = HttpClients.custom();

    if (isInsecureHttpsIngestion()) {
      acceptAllCertificates(builder);
    } else if (isHttpIngestion()) {
//      acceptHttpOnly(builder);
    }

    return builder.build();
  }

  private boolean isInsecureHttpsIngestion() {
    return ingestionClientConfig.url().startsWith("https://")
        && ingestionClientConfig.insecure();
  }

  private static void acceptAllCertificates(HttpClientBuilder builder)
      throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException {
    final TrustStrategy acceptingTrustStrategy = (cert, authType) -> true;
    final SSLContext sslContext = SSLContexts.custom()
        .loadTrustMaterial(null, acceptingTrustStrategy)
        .build();

    final SSLConnectionSocketFactory sslsf =
        new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
    final Registry<ConnectionSocketFactory> socketFactoryRegistry =
        RegistryBuilder.<ConnectionSocketFactory>create()
            .register(HTTP, new PlainConnectionSocketFactory())
            .register(HTTPS, sslsf)
            .build();

    final BasicHttpClientConnectionManager connectionManager =
        new BasicHttpClientConnectionManager(socketFactoryRegistry);

    builder.setConnectionManager(connectionManager);
  }

  private boolean isHttpIngestion() {
    return ingestionClientConfig.url().startsWith("http://");
  }

  private static void acceptHttpOnly(HttpClientBuilder builder) {
    final Registry<ConnectionSocketFactory> socketFactoryRegistry =
        RegistryBuilder.<ConnectionSocketFactory>create()
            .register(HTTP, new PlainConnectionSocketFactory())
            .build();

    final BasicHttpClientConnectionManager connectionManager =
        new BasicHttpClientConnectionManager(socketFactoryRegistry);

    builder.setConnectionManager(connectionManager);
  }
}
