package dev.streamx.cli.command.ingestion.stream;

import static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.responseDefinition;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.common.ContentTypes.APPLICATION_JSON;
import static com.github.tomakehurst.wiremock.common.ContentTypes.CONTENT_TYPE;
import static org.apache.hc.core5.http.HttpStatus.SC_ACCEPTED;
import static org.apache.hc.core5.http.HttpStatus.SC_BAD_REQUEST;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.matching.ContainsPattern;
import dev.streamx.cli.command.ingestion.AuthorizedProfile;
import dev.streamx.cli.command.ingestion.BaseIngestionCommandTest;
import dev.streamx.cli.command.ingestion.UnauthorizedProfile;
import dev.streamx.clients.ingestion.publisher.FailureResult;
import dev.streamx.clients.ingestion.publisher.IngestionResult;
import dev.streamx.clients.ingestion.publisher.SuccessResult;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainLauncher;
import io.quarkus.test.junit.main.QuarkusMainTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@QuarkusMainTest
class StreamCommandTest extends BaseIngestionCommandTest {

  private static final String CHANNEL = "pages";
  private static final String INVALID_PAYLOAD_REQUEST_CHANNEL = "bad-request-channel";
  private static final String UNSUPPORTED_CHANNEL = "images";
  private static final String KEY = "index.html";

  @Nested
  @QuarkusMainTest
  @TestProfile(UnauthorizedProfile.class)
  class UnauthorizedTest {

    @Test
    public void shouldPublishUsingIngestionClient(QuarkusMainLauncher launcher) {
      // when
      LaunchResult result = launcher.launch("stream",
          "--ingestion-url=" + getIngestionUrl(),
          CHANNEL,
          "target/test-classes/dev/streamx/cli/command/ingestion/stream/valid-json.stream");

      // then
      expectSuccess(result);
      wm.verify(postRequestedFor(urlEqualTo(getPublicationPath(CHANNEL)))
          .withRequestBody(matchingJsonPath("action", equalTo("publish")))
          .withoutHeader("Authorization"));
    }

    @Test
    public void shouldPublishToUnsupportedChannel(QuarkusMainLauncher launcher) {
      // when
      LaunchResult result = launcher.launch("stream",
          "--ingestion-url=" + getIngestionUrl(),
          UNSUPPORTED_CHANNEL,
          "target/test-classes/dev/streamx/cli/command/ingestion/stream/valid-json.stream");

      // then
      expectError(result, "Ingestion REST endpoint known error. "
                          + "Code: UNSUPPORTED_CHANNEL. Message: Channel images is unsupported. "
                          + "Supported channels: pages");
      wm.verify(postRequestedFor(urlEqualTo(getPublicationPath(UNSUPPORTED_CHANNEL)))
          .withRequestBody(matchingJsonPath("action", equalTo("publish")))
          .withoutHeader("Authorization"));
    }

    @Test
    public void shouldRejectInvalidJson(QuarkusMainLauncher launcher) {
      // when
      LaunchResult result = launcher.launch("stream",
          "--ingestion-url=" + getIngestionUrl(),
          CHANNEL,
          "target/test-classes/dev/streamx/cli/command/ingestion/stream/invalid-json.stream");

      // then
      expectError(result, "Payload could not be parsed.\n"
                          + "\n"
                          + "Supplied payload:\n"
                          + "Cannot parse JSON\n"
                          + "\n"
                          + "Make sure that:\n"
                          + " * it's valid JSON,\n"
                          + " * object property names are properly single-quoted (') "
                          + "or double-quoted (\"),\n"
                          + " * strings are properly single-quoted (') or double-quoted (\")\n"
                          + "\n"
                          + "Details: Unrecognized token 'ad': was expecting "
                          + "(JSON String, Number, Array, Object "
                          + "or token 'null', 'true' or 'false')\n"
                          + " at [Source: REDACTED "
                          + "(`StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION` disabled); "
                          + "line: 1, column: 14]");
    }

    @Test
    public void shouldRejectIllegalJson(QuarkusMainLauncher launcher) {
      // when
      LaunchResult result = launcher.launch("stream",
          "--ingestion-url=" + getIngestionUrl(),
          CHANNEL,
          "target/test-classes/dev/streamx/cli/command/ingestion/stream/illegal-json.stream");

      // then
      expectError(result, "Error performing stream publication using "
                          + "'target/test-classes/dev/streamx/cli/command/ingestion/"
                          + "stream/illegal-json.stream' file.\n"
                          + "\n"
                          + "Details:\n"
                          + "Missing or invalid 'action' field\n");
    }

    @Test
    public void shouldRejectInvalidJsonSeparator(QuarkusMainLauncher launcher) {
      // when
      LaunchResult result = launcher.launch("stream",
          "--ingestion-url=" + getIngestionUrl(),
          CHANNEL,
          "target/test-classes/dev/streamx/cli/command/ingestion/stream"
          + "/invalid-separated-json.stream"
      );

      // then
      expectError(result, "Payload could not be parsed.\n"
                          + "\n"
                          + "Supplied payload:\n"
                          + "Cannot parse JSON\n"
                          + "\n"
                          + "Make sure that:\n"
                          + " * it's valid JSON,\n"
                          + " * object property names are properly single-quoted (') "
                          + "or double-quoted (\"),\n"
                          + " * strings are properly single-quoted (') or double-quoted (\")\n"
                          + "\n"
                          + "Details: Unexpected character (',' (code 44)): expected a value\n"
                          + " at [Source: REDACTED ("
                          + "`StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION` disabled); "
                          + "line: 15, column: 2]");
    }
  }

  @Nested
  @QuarkusMainTest
  @TestProfile(AuthorizedProfile.class)
  class AuthorizedTest {

    @Test
    public void shouldPublishAuthorizedUsing(QuarkusMainLauncher launcher) {
      // when
      LaunchResult result = launcher.launch("stream",
          "--ingestion-url=" + getIngestionUrl(),
          CHANNEL,
          "target/test-classes/dev/streamx/cli/command/ingestion/stream/valid-json.stream");

      // then
      expectSuccess(result);
      wm.verify(postRequestedFor(urlEqualTo(getPublicationPath(CHANNEL)))
          .withRequestBody(matchingJsonPath("action", equalTo("publish")))
          .withHeader("Authorization", new ContainsPattern(AuthorizedProfile.AUTH_TOKEN)));
    }
  }

  @Override
  protected void initializeWiremock() {
    setupMockPublicationResponse(
        CHANNEL,
        SC_ACCEPTED,
        IngestionResult.of(new SuccessResult(123456L, KEY))
    );

    setupMockPublicationResponse(
        INVALID_PAYLOAD_REQUEST_CHANNEL,
        SC_BAD_REQUEST,
        IngestionResult.of(new FailureResult("INVALID_PUBLICATION_PAYLOAD", "Error message"))
    );

    setupMockPublicationResponse(
        UNSUPPORTED_CHANNEL,
        SC_BAD_REQUEST,
        new FailureResult("UNSUPPORTED_CHANNEL",
            "Channel " + UNSUPPORTED_CHANNEL + " is unsupported. Supported channels: " + CHANNEL
        )
    );

    setupMockChannelsSchemasResponse();
  }

  private static void setupMockPublicationResponse(String channel, int httpStatus,
      Object response) {
    ResponseDefinitionBuilder mockResponse = responseDefinition()
        .withStatus(httpStatus)
        .withBody(response == null ? null : Json.write(response))
        .withHeader(CONTENT_TYPE, APPLICATION_JSON);

    wm.stubFor(WireMock.post(getPublicationPath(channel))
        .willReturn(mockResponse));
  }
}
