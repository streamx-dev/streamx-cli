package dev.streamx.cli.command.ingestion.batch;

import static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.responseDefinition;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
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

public class BatchPublishCommandTest extends BaseIngestionCommandTest {

  private static final String CHANNEL = "pages";
  private static final String UNSUPPORTED_CHANNEL = "images";
  private static final String KEY = "index.html";

  @Nested
  @QuarkusMainTest
  @TestProfile(UnauthorizedProfile.class)
  class UnauthorizedTest {

    @Test
    public void shouldHandleInvalidPayloadFromRestIngestionApi(QuarkusMainLauncher launcher) {
      setupMockPublicationResponse(
          CHANNEL,
          SC_BAD_REQUEST,
          new FailureResult("INVALID_INGESTION_INPUT",
              "Data does not match the existing schema: Unknown union branch byte"
          )
      );

      // when
      LaunchResult result = launcher.launch(
          "batch", "--ingestion-url=" + getIngestionUrl(),
          "publish", "target/test-classes/dev/streamx/cli/command/ingestion/batch/invalid-channel"
      );

      // then
      expectError(result, """
          Error performing batch publication while processing \
          'target/test-classes/dev/streamx/cli/command/ingestion/batch/invalid-channel/index.html' \
          file.
          
          Details:
          Ingestion REST endpoint known error. Code: INVALID_INGESTION_INPUT. \
          Message: Data does not match the existing schema: Unknown union branch byte
          
          Full logs can be found in quarkus.log""");
    }

    @Test
    public void shouldRejectUnknownChannel(QuarkusMainLauncher launcher) {
      // when
      LaunchResult result = launcher.launch(
          "batch", "--ingestion-url=" + getIngestionUrl(),
          "publish", "target/test-classes/dev/streamx/cli/command/ingestion/batch/unknown-channel"
      );

      // then
      expectError(result,
          "Channel 'images' not found. Available channels: [pages]");
    }

    @Test
    public void shouldRejectInvalidDataJson(QuarkusMainLauncher launcher) {
      // when
      LaunchResult result = launcher.launch(
          "batch", "--ingestion-url=" + getIngestionUrl(),
          "publish", "target/test-classes/dev/streamx/cli/command/ingestion/batch/invalid-json"
      );

      // then
      expectError(result,
          """
              Could not resolve payload for file '\
              target/test-classes/dev/streamx/cli/command/ingestion/batch/invalid-json/content.json'
              
              Details:
              Unrecognized token 'ad': was expecting (JSON String, Number, Array, Object \
              or token 'null', 'true' or 'false')
               at [Source: REDACTED (`StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION` disabled); \
              line: 1, column: 14]
              
              Full logs can be found in quarkus.log""");
    }

    @Test
    public void shouldBatchPublishValidDirectory(QuarkusMainLauncher launcher) {
      // when
      LaunchResult result = launcher.launch(
          "batch", "--ingestion-url=" + getIngestionUrl(),
          "publish", "target/test-classes/dev/streamx/cli/command/ingestion/batch/valid"
      );

      // then
      expectSuccess(result);
      wm.verify(postRequestedFor(urlEqualTo(getPublicationPath(CHANNEL)))
          .withRequestBody(equalToJson("""
              {
                "key" : "valid/index.html",
                "action" : "publish",
                "eventTime" : null,
                "properties" : {
                  "sx:type" : "page/sub-page"
                },
                "payload" : {
                  "dev.streamx.blueprints.data.Page" : {
                    "content" : {
                      "bytes" : "<h1>Hello World!</h1>\\n"
                    }
                  }
                }
              }
              """))
          .withoutHeader("Authorization"));
      wm.verify(1, postRequestedFor(urlEqualTo(getPublicationPath(CHANNEL))));
    }

    @Test
    public void shouldRejectWrongIngestionUrl(QuarkusMainLauncher launcher) {
      // when
      String ingestionServiceUrl = "http://aaa.bbb.ccc";
      LaunchResult result = launcher.launch(
          "batch",
          "--ingestion-url=" + ingestionServiceUrl,
          "publish", "target/test-classes/dev/streamx/cli/command/ingestion/batch/valid");

      // then
      expectError(result,
          """
              Unable to connect to the ingestion service.
                            
              The ingestion service URL: http://aaa.bbb.ccc
                            
              Verify:
               * if the mesh is up and running,
               * if the ingestion service URL is set correctly\
               (if it's not - set proper '--ingestion-url' option)
               
              Full logs can be found in"""
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
      LaunchResult result = launcher.launch("batch",
          "--ingestion-url=" + getIngestionUrl(),
          "publish", "target/test-classes/dev/streamx/cli/command/ingestion/batch/valid");

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
