package dev.streamx.cli.command.ingestion;

import static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.responseDefinition;
import static com.github.tomakehurst.wiremock.common.ContentTypes.CONTENT_TYPE;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.streamx.clients.ingestion.StreamxClient.INGESTION_ENDPOINT_PATH_V2;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.streamx.ce.serialization.json.CloudEventJsonSerializer;
import io.cloudevents.CloudEvent;
import io.quarkus.test.junit.main.LaunchResult;
import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;

public abstract class BaseIngestionCommandTest {

  protected static final String PUBLICATION_PATH = INGESTION_ENDPOINT_PATH_V2;

  @RegisterExtension
  protected static WireMockExtension wm = WireMockExtension.newInstance()
      .options(wireMockConfig().dynamicPort())
      .configureStaticDsl(true)
      .build();

  @BeforeEach
  void setup() {
    initializeWiremock();
  }

  private void initializeWiremock() {
    setupMockPublicationResponse(
        CloudEventBuilder.build("index.html", "some-event-type", "source", "mock-response")
    );
  }

  protected static String getIngestionUrl() {
    return "http://localhost:" + wm.getPort();
  }

  protected static void expectSuccess(LaunchResult result) {
    assertThat(result.exitCode()).isZero();
    assertThat(result.getErrorOutput()).isEmpty();
  }

  protected static void expectError(LaunchResult result, String expectedErrorOutput) {
    assertThat(result.exitCode()).isNotZero();
    assertThat(result.getErrorOutput().replace("\r\n", "\n")).isEqualTo(expectedErrorOutput);
  }

  protected static void setupMockPublicationResponse(CloudEvent response) {
    ResponseDefinitionBuilder mockResponse = responseDefinition()
        .withStatus(HttpStatus.SC_ACCEPTED)
        .withBody(new CloudEventJsonSerializer().serialize(response))
        .withHeader(CONTENT_TYPE, "application/cloudevents+json");

    wm.stubFor(WireMock.post(PUBLICATION_PATH)
        .willReturn(mockResponse));
  }
}
