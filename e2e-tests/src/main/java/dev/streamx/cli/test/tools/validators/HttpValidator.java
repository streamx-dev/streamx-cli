package dev.streamx.cli.test.tools.validators;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.awaitility.Awaitility.await;

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.IOException;
import java.time.Duration;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.assertj.core.api.ThrowingConsumer;
import org.jboss.logging.Logger;

@ApplicationScoped
public class HttpValidator {

  private final Logger logger = Logger.getLogger(HttpValidator.class);

  @Inject
  CloseableHttpClient httpClient;

  public void validate(String url, int expectedStatusCode, String expectedBody, Duration timeout) {
    validate(url, expectedStatusCode, timeout, httpEntity -> {
      String responseBody = EntityUtils.toString(httpEntity);
      assertThat(responseBody).describedAs(url).contains(expectedBody);
    });
  }

  public void validate(String url, int expectedStatusCode, byte[] expectedBody, Duration timeout) {
    validate(url, expectedStatusCode, timeout, httpEntity -> {
      byte[] responseBody = EntityUtils.toByteArray(httpEntity);
      assertThat(responseBody).describedAs(url).containsExactly(expectedBody);
    });
  }

  public void validate(String url, int expectedStatusCode, Duration timeout,
      ThrowingConsumer<HttpEntity> responseEntityAssertion) {
    await()
        .atMost(timeout)
        .pollInterval(100, MILLISECONDS)
        .untilAsserted(() -> {
          HttpGet request = new HttpGet(url);
          try (CloseableHttpResponse response = httpClient.execute(request)) {
            int statusCode = response.getStatusLine().getStatusCode();
            assertThat(statusCode).describedAs(url).isEqualTo(expectedStatusCode);
            HttpEntity responseEntity = response.getEntity();
            responseEntityAssertion.accept(responseEntity);
          } catch (IOException e) {
            fail("Request to " + url + "failed", e);
          }
        });
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
