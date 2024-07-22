package dev.streamx.cli.test.tools.validators;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.IOException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.jboss.logging.Logger;

@ApplicationScoped
public class HttpValidator {

  private final Logger logger = Logger.getLogger(HttpValidator.class);

  @Inject
  CloseableHttpClient httpClient;

  public void validate(String url, int expectedStatusCode, String expectedBody, int timeout) {
    await()
        .atMost(timeout, SECONDS)
        .alias("Assertion of response from url: " + url
            + " with expecting status:" + expectedStatusCode
            + " body:" + expectedBody
            + " timeout:" + timeout)
        .pollInterval(100, MILLISECONDS)
        .until(() ->
            validate(url, expectedStatusCode, expectedBody)
        );
  }

  private boolean validate(String url, int expectedStatusCode, String expectedBody) {
    HttpGet request = new HttpGet(url);
    try (CloseableHttpResponse response = httpClient.execute(request)) {
      int actualStatusCode = response.getStatusLine().getStatusCode();
      String responseBody = EntityUtils.toString(response.getEntity());
      logger.info("Request to " + url
          + " return statusCode " + actualStatusCode
          + " and body " + responseBody);
      return actualStatusCode == expectedStatusCode && responseBody.contains(expectedBody);
    } catch (IOException e) {
      logger.error("Request to " + url + "failed: " + e.getMessage(), e);
      throw new RuntimeException("Can not make request:" + url, e);
    }
  }

  @PreDestroy
  public void cleanUp() {
    try {
      httpClient.close();
      logger.info("HttpClient closed");
    } catch (IOException e) {
      logger.error("Can not close http client", e);
    }
  }
}
