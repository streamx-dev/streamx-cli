package dev.streamx.cli.ingestion.unpublish;

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

public class UnpublishCommandTest extends BaseIngestionCommandTest {

  private static final String CHANNEL = "pages";
  private static final String UNSUPPORTED_CHANNEL = "images";
  private static final String KEY = "index.html";

  @Nested
  @QuarkusMainTest
  @TestProfile(UnauthorizedProfile.class)
  class UnauthorizedTest {

    @Test
    public void shouldUnpublishUsingIngestionClient(QuarkusMainLauncher launcher) {
      // when
      LaunchResult result = launcher.launch("unpublish",
          "--ingestion-url=" + getIngestionUrl(),
          CHANNEL, KEY);

      // then
      expectSuccess(result);
    }

    @Test
    public void shouldUnpublishUsingUnauthorizedIngestionClient(QuarkusMainLauncher launcher) {
      // when
      LaunchResult result = launcher.launch("unpublish",
          "--ingestion-url=" + getIngestionUrl(),
          CHANNEL, KEY);

      // then
      expectSuccess(result);

      wm.verify(postRequestedFor(urlEqualTo(getPublicationPath(CHANNEL)))
          .withRequestBody(matchingJsonPath("action", equalTo("unpublish")))
          .withoutHeader("Authorization"));
    }

    @Test
    public void shouldRejectUnknownChannel(QuarkusMainLauncher launcher) {
      // when
      LaunchResult result = launcher.launch("unpublish",
          "--ingestion-url=" + getIngestionUrl(),
          UNSUPPORTED_CHANNEL, KEY);

      // then
      expectError(result,
          "Ingestion REST endpoint known error. Code: UNSUPPORTED_CHANNEL. "
          + "Message: Channel images is unsupported. Supported channels: pages");
    }
  }

  @Nested
  @QuarkusMainTest
  @TestProfile(AuthorizedProfile.class)
  class AuthorizedTest {

    @Test
    public void shouldUnpublishUsingAuthorizedIngestionClient(QuarkusMainLauncher launcher) {
      // when
      LaunchResult result = launcher.launch("unpublish",
          "--ingestion-url=" + getIngestionUrl(),
          CHANNEL, KEY);

      // then
      expectSuccess(result);

      wm.verify(
          postRequestedFor(urlEqualTo(getPublicationPath(CHANNEL)))
              .withRequestBody(equalToJson("""
                  {
                    "key": "%s",
                    "payload": null,
                    "action": "unpublish"
                  }
              """.formatted(KEY), true, true))
              .withHeader("Authorization", new ContainsPattern(AuthorizedProfile.AUTH_TOKEN)));
    }
  }

  @Override
  protected void initializeWiremock() {
    setupMockResponse(
        CHANNEL,
        SC_ACCEPTED,
        new SuccessResult(123456L, KEY)
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
        .withBody(Json.write(response))
        .withHeader(CONTENT_TYPE, APPLICATION_JSON);

    wm.stubFor(WireMock.post(getPublicationPath(channel))
        .willReturn(mockResponse));
  }
}
