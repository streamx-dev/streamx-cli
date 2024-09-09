package dev.streamx.cli.ingestion;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static dev.streamx.clients.ingestion.StreamxClient.PUBLICATIONS_ENDPOINT_PATH_V1;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import io.quarkus.test.junit.main.LaunchResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;

public abstract class BaseIngestionCommandTest {

  @RegisterExtension
  protected static WireMockExtension wm = WireMockExtension.newInstance()
      .options(wireMockConfig().dynamicPort())
      .configureStaticDsl(true)
      .build();

  @BeforeEach
  void setup() {
    initializeWiremock();
  }

  protected abstract void initializeWiremock();

  protected static String getIngestionUrl() {
    return "http://localhost:" + wm.getPort();
  }

  protected static String getPublicationPath(String channel, String key) {
    return PUBLICATIONS_ENDPOINT_PATH_V1 + "/" + channel + "/" + key;
  }

  protected static void expectSuccess(LaunchResult result) {
    assertThat(result.exitCode()).isZero();
    assertThat(result.getErrorOutput()).isEmpty();
  }

  protected static void expectError(LaunchResult result, String expectedErrorOutput) {
    assertThat(result.exitCode()).isNotZero();
    assertThat(result.getErrorOutput()).isEqualTo(expectedErrorOutput);
  }
}
