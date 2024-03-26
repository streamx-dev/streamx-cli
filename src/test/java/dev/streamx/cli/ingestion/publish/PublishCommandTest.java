package dev.streamx.cli.ingestion.publish;

import static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.responseDefinition;
import static com.github.tomakehurst.wiremock.common.ContentTypes.APPLICATION_JSON;
import static com.github.tomakehurst.wiremock.common.ContentTypes.CONTENT_TYPE;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static dev.streamx.clients.ingestion.StreamxClient.PUBLICATIONS_ENDPOINT_PATH_V1;
import static org.apache.hc.core5.http.HttpStatus.SC_ACCEPTED;
import static org.apache.hc.core5.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.hc.core5.http.HttpStatus.SC_OK;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import dev.streamx.clients.ingestion.impl.FailureResponse;
import dev.streamx.clients.ingestion.publisher.PublisherSuccessResult;
import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainLauncher;
import io.quarkus.test.junit.main.QuarkusMainTest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

@QuarkusMainTest
public class PublishCommandTest {
  private static final String CHANNEL = "pages";
  private static final String BAD_REQUEST_CHANNEL = "bad-request-channel";
  private static final String KEY = "index.html";
  private static final String DATA = """
        {"content": {"bytes": "<h1>Hello World!</h1>"}}""";

  @RegisterExtension
  static WireMockExtension wm = WireMockExtension.newInstance()
      .options(wireMockConfig().dynamicPort()).configureStaticDsl(true).build();

  @BeforeEach
  void setup() {
    initializeWiremock();
  }

  @Test
  public void shouldHandleBadRequestFromRestIngestionApi(QuarkusMainLauncher launcher) {
    // when
    LaunchResult result = launcher.launch("publish",
        "--ingestion-url=" + getIngestionUrl(),
        "--data=" + DATA,
        BAD_REQUEST_CHANNEL, KEY);

    // then
    assertThat(result.exitCode()).isNotZero();
    assertThat(result.getErrorOutput()).contains("Publication Ingestion REST endpoint known error. Code: INVALID_PUBLICATION_PAYLOAD. Message: Error message");
  }

  @Test
  public void shouldRejectInvalidDataJson(QuarkusMainLauncher launcher) {
    // given
    String invalidJson = "asdf{][";

    // when
    LaunchResult result = launcher.launch("publish",
        "--ingestion-url=" + getIngestionUrl(),
        "--data=" + invalidJson,
        BAD_REQUEST_CHANNEL, KEY);

    // then
    assertThat(result.exitCode()).isNotZero();
    assertThat(result.getErrorOutput()).contains("Payload could not be parsed.");
  }

  @Test
  public void shouldPublishUsingIngestionClient(QuarkusMainLauncher launcher) {
    // when
    LaunchResult result = launcher.launch("publish",
        "--ingestion-url=" + getIngestionUrl(),
        "--data=" + DATA,
        CHANNEL, KEY);

    // then
    assertThat(result.exitCode()).isZero();
  }

  @Test
  public void shouldRejectUnknownChannel(QuarkusMainLauncher launcher) {
    // given
    String channel = "channel";

    // when
    LaunchResult result = launcher.launch("publish",
        "--ingestion-url=" + getIngestionUrl(),
        "--data=" + DATA,
        channel, KEY);

    // then
    assertThat(result.getErrorOutput()).containsSubsequence("Channel", "not found");
    assertThat(result.exitCode()).isNotZero();
  }

  private static void initializeWiremock() {
    stubSchemas();
    stubPublication();
  }

  private static void stubSchemas() {
    String response = """
        {"pages":{"type":"record","name":"Page","namespace":"dev.streamx.blueprints.data","fields":[{"name":"content","type":["null","bytes"],"default":null}]},"bad-request-channel":{"type":"record","name":"Whatever","namespace":"dev.streamx.blueprints.data","fields":[]}}""";

    wm.stubFor(WireMock.get(getSchema())
        .willReturn(responseDefinition().withStatus(SC_OK).withBody(response)
            .withHeader(CONTENT_TYPE, APPLICATION_JSON)
        )
    );
  }

  private static void stubPublication() {
    PublisherSuccessResult result = new PublisherSuccessResult(123456L);
    wm.stubFor(WireMock.put(getPublicationPath(PublishCommandTest.CHANNEL, PublishCommandTest.KEY))
        .willReturn(responseDefinition().withStatus(SC_ACCEPTED).withBody(Json.write(result))
            .withHeader(CONTENT_TYPE, APPLICATION_JSON)));

    FailureResponse badRequest  = new FailureResponse("INVALID_PUBLICATION_PAYLOAD", "Error message");
    wm.stubFor(WireMock.put(getPublicationPath(PublishCommandTest.BAD_REQUEST_CHANNEL, PublishCommandTest.KEY))
        .willReturn(responseDefinition().withStatus(SC_BAD_REQUEST).withBody(Json.write(badRequest))
            .withHeader(CONTENT_TYPE, APPLICATION_JSON)));
  }

  @NotNull
  private static String getIngestionUrl() {
    return "http://localhost:" + wm.getPort();
  }

  private static String getSchema() {
    return PUBLICATIONS_ENDPOINT_PATH_V1 + "/schema";
  }

  private static String getPublicationPath(String channel, String key) {
    return PUBLICATIONS_ENDPOINT_PATH_V1 + "/" + channel + "/" + key;
  }
}
