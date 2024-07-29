package dev.streamx.cli.ingestion.publish;

import static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.responseDefinition;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.common.ContentTypes.APPLICATION_JSON;
import static com.github.tomakehurst.wiremock.common.ContentTypes.CONTENT_TYPE;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static dev.streamx.clients.ingestion.StreamxClient.PUBLICATIONS_ENDPOINT_PATH_V1;
import static org.apache.hc.core5.http.HttpStatus.SC_ACCEPTED;
import static org.apache.hc.core5.http.HttpStatus.SC_BAD_REQUEST;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.matching.ContainsPattern;
import dev.streamx.cli.ingestion.AuthorizedProfile;
import dev.streamx.cli.ingestion.UnauthorizedProfile;
import dev.streamx.clients.ingestion.impl.FailureResponse;
import dev.streamx.clients.ingestion.publisher.PublisherSuccessResult;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainLauncher;
import io.quarkus.test.junit.main.QuarkusMainTest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class PublishCommandTest {

  private static final String CHANNEL = "pages";
  private static final String INVALID_PAYLOAD_REQUEST_CHANNEL = "bad-request-channel";
  private static final String UNSUPPORTED_CHANNEL = "images";
  private static final String KEY = "index.html";
  private static final String DATA = """
      {"content": {"bytes": "<h1>Hello World!</h1>"}}""";
  private static final String PAYLOAD_PATH =
      "target/test-classes/dev/streamx/cli/publish/payload/helloworld-payload.json";

  @RegisterExtension
  static WireMockExtension wm = WireMockExtension.newInstance()
      .options(wireMockConfig().dynamicPort()).configureStaticDsl(true).build();

  @BeforeEach
  void setup() {
    initializeWiremock();
  }

  @Nested
  @QuarkusMainTest
  @TestProfile(UnauthorizedProfile.class)
  class UnauthorizedTest {

    @Test
    public void shouldHandleInvalidPayloadFromRestIngestionApi(QuarkusMainLauncher launcher) {
      // when
      LaunchResult result = launcher.launch("publish",
          "--ingestion-url=" + getIngestionUrl(),
          "-j=" + DATA,
          INVALID_PAYLOAD_REQUEST_CHANNEL, KEY);

      // then
      assertThat(result.exitCode()).isNotZero();
      assertThat(result.getErrorOutput()).contains(
          "Publication Ingestion REST endpoint known error. "
              + "Code: INVALID_PUBLICATION_PAYLOAD. "
              + "Message: Error message");
    }

    @Test
    public void shouldRejectInvalidDataJson(QuarkusMainLauncher launcher) {
      // given
      String invalidJson = "asdf{][";

      // when
      LaunchResult result = launcher.launch("publish",
          "--ingestion-url=" + getIngestionUrl(),
          "-j=" + invalidJson,
          CHANNEL, KEY);

      // then
      assertThat(result.exitCode()).isNotZero();
      assertThat(result.getErrorOutput()).contains("Payload could not be parsed.");
    }

    @Test
    public void shouldPublishUsingIngestionClient(QuarkusMainLauncher launcher) {
      // when
      LaunchResult result = launcher.launch("publish",
          "--ingestion-url=" + getIngestionUrl(),
          "-j=" + DATA,
          CHANNEL, KEY);

      // then
      assertThat(result.exitCode()).isZero();
    }

    @Test
    public void shouldPublishUnauthorizedData(QuarkusMainLauncher launcher) {
      // when
      LaunchResult result = launcher.launch("publish",
          "--ingestion-url=" + getIngestionUrl(),
          "-j=" + DATA,
          CHANNEL, KEY);

      // then
      assertThat(result.exitCode()).isZero();
      wm.verify(putRequestedFor(urlEqualTo(getPublicationPath(CHANNEL, KEY)))
          .withoutHeader("Authorization"));
    }

    @Test
    public void shouldPublishBinaryData(QuarkusMainLauncher launcher) {
      // when
      LaunchResult result = launcher.launch("publish",
          "--ingestion-url=" + getIngestionUrl(),
          "-b=content='<h1>Hello World!</h1>'",
          CHANNEL, KEY);

      // then
      assertThat(result.exitCode()).isZero();
    }

    @Test
    public void shouldPublishUsingPayloadFromPayloadArg(QuarkusMainLauncher launcher) {
      // when
      LaunchResult result = launcher.launch("publish",
          "--ingestion-url=" + getIngestionUrl(),
          CHANNEL, KEY, PAYLOAD_PATH);

      // then
      assertThat(result.exitCode()).isZero();
    }

    @Test
    public void shouldRejectUnknownChannel(QuarkusMainLauncher launcher) {
      // when
      LaunchResult result = launcher.launch("publish",
          "--ingestion-url=" + getIngestionUrl(),
          "-j=" + DATA,
          UNSUPPORTED_CHANNEL, KEY);

      // then
      assertThat(result.getErrorOutput()).isEqualTo(
          "Publication Ingestion REST endpoint known error. Code: UNSUPPORTED_CHANNEL. "
          + "Message: Channel images is unsupported. Supported channels: pages");
      assertThat(result.exitCode()).isNotZero();
    }

    @Test
    public void shouldRejectWrongIngestionUrl(QuarkusMainLauncher launcher) {
      // when
      String ingestionServiceUrl = "http://aaa.bbb.ccc";
      LaunchResult result = launcher.launch("publish",
          "--ingestion-url=" + ingestionServiceUrl,
          "-j=" + DATA,
          CHANNEL, KEY);

      // then
      assertThat(result.getErrorOutput().lines()).contains(
          "Unable to connect to the ingestion service.",
          "The ingestion service URL: " + ingestionServiceUrl,
          "Verify:"
      );
      assertThat(result.exitCode()).isNotZero();
    }
  }

  @Nested
  @QuarkusMainTest
  @TestProfile(AuthorizedProfile.class)
  class AuthorizedTest {

    @Test
    public void shouldPublishAuthorizedUsing(QuarkusMainLauncher launcher) {
      // when
      LaunchResult result = launcher.launch("publish",
          "--ingestion-url=" + getIngestionUrl(),
          "-j=" + DATA,
          CHANNEL, KEY);

      // then
      assertThat(result.exitCode()).isZero();
      wm.verify(putRequestedFor(urlEqualTo(getPublicationPath(CHANNEL, KEY)))
          .withHeader("Authorization", new ContainsPattern(AuthorizedProfile.AUTH_TOKEN)));
    }
  }

  private static void initializeWiremock() {
    stubPublication();
  }

  private static void stubPublication() {
    setupMockResponse(
        CHANNEL,
        SC_ACCEPTED,
        new PublisherSuccessResult(123456L)
    );

    setupMockResponse(
        INVALID_PAYLOAD_REQUEST_CHANNEL,
        SC_BAD_REQUEST,
        new FailureResponse("INVALID_PUBLICATION_PAYLOAD", "Error message")
    );

    setupMockResponse(
        UNSUPPORTED_CHANNEL,
        SC_BAD_REQUEST,
        new FailureResponse("UNSUPPORTED_CHANNEL",
            "Channel " + UNSUPPORTED_CHANNEL + " is unsupported. Supported channels: " + CHANNEL
        )
    );
  }

  private static void setupMockResponse(String channel, int httpStatus, Object response) {
    ResponseDefinitionBuilder mockResponse = responseDefinition()
        .withStatus(httpStatus)
        .withBody(response == null ? null : Json.write(response))
        .withHeader(CONTENT_TYPE, APPLICATION_JSON);

    wm.stubFor(WireMock.put(getPublicationPath(channel, KEY))
        .willReturn(mockResponse));
  }

  @NotNull
  private static String getIngestionUrl() {
    return "http://localhost:" + wm.getPort();
  }

  private static String getPublicationPath(String channel, String key) {
    return PUBLICATIONS_ENDPOINT_PATH_V1 + "/" + channel + "/" + key;
  }
}
