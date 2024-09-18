package dev.streamx.cli.ingestion.publish;

import static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.responseDefinition;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.common.ContentTypes.APPLICATION_JSON;
import static com.github.tomakehurst.wiremock.common.ContentTypes.CONTENT_TYPE;
import static org.apache.hc.core5.http.HttpStatus.SC_ACCEPTED;
import static org.apache.hc.core5.http.HttpStatus.SC_BAD_REQUEST;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.matching.ContainsPattern;
import dev.streamx.cli.ingestion.AuthorizedProfile;
import dev.streamx.cli.ingestion.BaseIngestionCommandTest;
import dev.streamx.cli.ingestion.UnauthorizedProfile;
import dev.streamx.clients.ingestion.impl.FailureResponse;
import dev.streamx.clients.ingestion.publisher.SuccessResult;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainLauncher;
import io.quarkus.test.junit.main.QuarkusMainTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class PublishCommandTest extends BaseIngestionCommandTest {

  private static final String CHANNEL = "pages";
  private static final String INVALID_PAYLOAD_REQUEST_CHANNEL = "bad-request-channel";
  private static final String UNSUPPORTED_CHANNEL = "images";
  private static final String KEY = "index.html";
  private static final String DATA = """
      {"content": {"bytes": "<h1>Hello World!</h1>"}}""";
  private static final String PAYLOAD_PATH =
      "target/test-classes/dev/streamx/cli/publish/payload/helloworld-payload.json";

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
      expectError(result,
          "Channel 'bad-request-channel' not found. Available channels: [pages]");
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
      expectError(result, """
          Payload could not be parsed.
                    
          Supplied payload:
          asdf{][
                    
          Make sure that:
           * it's valid JSON,
           * object property names are properly single-quoted (') or double-quoted ("),
           * strings are properly single-quoted (') or double-quoted (")
                    
          Details: Unrecognized token 'asdf': was expecting (JSON String, Number, Array, Object\
           or token 'null', 'true' or 'false')
           at [Source: REDACTED (`StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION` disabled);\
           line: 1, column: 6]""");
    }

    @Test
    public void shouldPublishUsingIngestionClient(QuarkusMainLauncher launcher) {
      // when
      LaunchResult result = launcher.launch("publish",
          "--ingestion-url=" + getIngestionUrl(),
          "-j=" + DATA,
          CHANNEL, KEY);

      // then
      expectSuccess(result);
      wm.verify(putRequestedFor(urlEqualTo(getPublicationPath(CHANNEL)))
          .withRequestBody(matchingJsonPath("$.action", equalTo("publish")))
          .withoutHeader("Authorization"));
    }

    @Test
    public void shouldPublishUnauthorizedData(QuarkusMainLauncher launcher) {
      // when
      LaunchResult result = launcher.launch("publish",
          "--ingestion-url=" + getIngestionUrl(),
          "-j=" + DATA,
          CHANNEL, KEY);

      // then
      expectSuccess(result);
      wm.verify(putRequestedFor(urlEqualTo(getPublicationPath(CHANNEL)))
          .withRequestBody(equalToJson("""
              {
                "key" : "index.html",
                "action" : "publish",
                "eventTime" : null,
                "properties" : { },
                "payload" : {
                  "dev.streamx.blueprints.data.Page" : {
                    "content" : {
                      "bytes" : "<h1>Hello World!</h1>"
                    }
                  }
                }
              }
              """))
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
      expectSuccess(result);
    }

    @Test
    public void shouldPublishUsingPayloadFromPayloadArg(QuarkusMainLauncher launcher) {
      // when
      LaunchResult result = launcher.launch("publish",
          "--ingestion-url=" + getIngestionUrl(),
          CHANNEL, KEY, PAYLOAD_PATH);

      // then
      expectSuccess(result);
    }

    @Test
    public void shouldRejectUnknownChannel(QuarkusMainLauncher launcher) {
      // when
      LaunchResult result = launcher.launch("publish",
          "--ingestion-url=" + getIngestionUrl(),
          "-j=" + DATA,
          UNSUPPORTED_CHANNEL, KEY);

      // then
      expectError(result,
          "Channel 'images' not found. Available channels: [pages]");
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
      expectError(result,
          """
              Unable to connect to the ingestion service.
                            
              The ingestion service URL: http://aaa.bbb.ccc
                            
              Verify:
               * if the mesh is up and running,
               * if the ingestion service URL is set correctly\
               (if it's not - set proper '--ingestion-url' option)"""
      );
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
      expectSuccess(result);
      wm.verify(putRequestedFor(urlEqualTo(getPublicationPath(CHANNEL)))
          .withRequestBody(matchingJsonPath("$.action", equalTo("publish")))
          .withHeader("Authorization", new ContainsPattern(AuthorizedProfile.AUTH_TOKEN)));
    }
  }

  @Override
  protected void initializeWiremock() {
    setupMockPublicationResponse(
        CHANNEL,
        SC_ACCEPTED,
        new SuccessResult(123456L, KEY)
    );

    setupMockPublicationResponse(
        INVALID_PAYLOAD_REQUEST_CHANNEL,
        SC_BAD_REQUEST,
        new FailureResponse("INVALID_PUBLICATION_PAYLOAD", "Error message")
    );

    setupMockPublicationResponse(
        UNSUPPORTED_CHANNEL,
        SC_BAD_REQUEST,
        new FailureResponse("UNSUPPORTED_CHANNEL",
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

    wm.stubFor(WireMock.put(getPublicationPath(channel))
        .willReturn(mockResponse));
  }
}
