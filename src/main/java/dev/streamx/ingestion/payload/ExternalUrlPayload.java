package dev.streamx.ingestion.payload;

import dev.streamx.exception.GitHubActionException;
import dev.streamx.ingestion.IngestionPayload;
import io.cloudevents.CloudEvent;
import java.io.IOException;
import java.util.Objects;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.jboss.logging.Logger;

public class ExternalUrlPayload implements IngestionPayload {

  private static final Logger log = Logger.getLogger(ExternalUrlPayload.class);

  private static final int DEFAULT_SOCKET_TIMEOUT = 1000;
  private static final int DEFAULT_CONNECTION_TIMEOUT = 5000;

  private final CloseableHttpClient httpClient;
  private final String eventType;
  private final String url;

  private String key;
  private int socketTimeout = DEFAULT_SOCKET_TIMEOUT;
  private int connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;

  public ExternalUrlPayload(CloseableHttpClient httpClient, String eventType, String url) {
    this.httpClient = httpClient;
    this.eventType = eventType;
    this.url = url;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public void setSocketTimeout(int socketTimeout) {
    this.socketTimeout = socketTimeout;
  }

  public void setConnectionTimeout(int connectionTimeout) {
    this.connectionTimeout = connectionTimeout;
  }

  @Override
  public CloudEvent resolve() throws GitHubActionException {
    byte[] bytes = requestBytes();
    if (log.isDebugEnabled()) {
      log.debugf("Read external resource: %s, bytes length: %d", url, bytes.length);
    }
    String subject = Objects.toString(key, url);
    CloudEvent event = CloudEventFactory.createPublishEvent(eventType, subject, bytes);
    if (log.isDebugEnabled()) {
      log.debugf("CloudEvent: type=%s, subject=%s", event.getType(), event.getSubject());
    }
    return event;
  }

  private byte[] requestBytes() throws GitHubActionException {
    byte[] byteArray;
    HttpGet httpRequest = new HttpGet(url);
    httpRequest.setConfig(RequestConfig.copy(RequestConfig.DEFAULT)
        .setConnectTimeout(connectionTimeout)
        .setSocketTimeout(socketTimeout)
        .build());
    try (CloseableHttpResponse response = httpClient.execute(httpRequest)) {
      byteArray = EntityUtils.toByteArray(response.getEntity());
    } catch (IOException exc) {
      log.error(exc.getMessage(), exc);
      throw new GitHubActionException(String.format(
          "Request for external resource %s data has failed: %s", url, exc.getMessage()));
    }
    return byteArray;
  }

}
