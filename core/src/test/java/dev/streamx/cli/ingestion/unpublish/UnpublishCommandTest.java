package dev.streamx.cli.ingestion.unpublish;

import static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.responseDefinition;
import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.common.ContentTypes.APPLICATION_JSON;
import static com.github.tomakehurst.wiremock.common.ContentTypes.CONTENT_TYPE;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static dev.streamx.clients.ingestion.StreamxClient.PUBLICATIONS_ENDPOINT_PATH_V1;
import static org.apache.hc.core5.http.HttpStatus.SC_ACCEPTED;
import static org.apache.hc.core5.http.HttpStatus.SC_OK;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.matching.ContainsPattern;
import dev.streamx.cli.ingestion.AuthorizedProfile;
import dev.streamx.cli.ingestion.UnauthorizedProfile;
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

public class UnpublishCommandTest {

  private static final String CHANNEL = "pages";
  private static final String KEY = "index.html";

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
    public void shouldUnpublishUsingIngestionClient(QuarkusMainLauncher launcher) {
      // when
      LaunchResult result = launcher.launch("unpublish",
          "--ingestion-url=" + getIngestionUrl(),
          CHANNEL, KEY);

      // then
      assertThat(result.exitCode()).isZero();
    }

    @Test
    public void shouldUnpublishUsingUnauthorizedIngestionClient(QuarkusMainLauncher launcher) {
      // when
      LaunchResult result = launcher.launch("unpublish",
          "--ingestion-url=" + getIngestionUrl(),
          CHANNEL, KEY);

      // then
      assertThat(result.exitCode()).isZero();

      wm.verify(getRequestedFor(urlEqualTo(getSchema()))
          .withoutHeader("Authorization"));
      wm.verify(deleteRequestedFor(urlEqualTo(getPublicationPath(CHANNEL, KEY)))
          .withoutHeader("Authorization"));
    }

    @Test
    public void shouldRejectUnknownChannel(QuarkusMainLauncher launcher) {
      // given
      String channel = "channel";

      // when
      LaunchResult result = launcher.launch("unpublish",
          "--ingestion-url=" + getIngestionUrl(),
          channel, KEY);

      // then
      assertThat(result.getErrorOutput()).containsSubsequence("Channel", "not found");
      assertThat(result.exitCode()).isNotZero();
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
      assertThat(result.exitCode()).isZero();

      wm.verify(getRequestedFor(urlEqualTo(getSchema()))
          .withHeader("Authorization", new ContainsPattern(AuthorizedProfile.JWT_TOKEN)));
      wm.verify(deleteRequestedFor(urlEqualTo(getPublicationPath(CHANNEL, KEY)))
          .withHeader("Authorization", new ContainsPattern(AuthorizedProfile.JWT_TOKEN)));
    }
  }

  private static void initializeWiremock() {
    stubSchemas();
    stubUnpublication();
  }

  private static void stubSchemas() {
    String response = "{\"pages\":{\"type\":\"record\",\"name\":\"Page\",\"namespace\":"
                      + "\"dev.streamx.blueprints.data\",\"fields\":[{\"name\":\"content\","
                      + "\"type\":[\"null\",\"bytes\"],\"default\":null}]},"
                      + "\"bad-request-channel\":{\"type\":\"record\",\"name\":\"Whatever\","
                      + "\"namespace\":\"dev.streamx.blueprints.data\",\"fields\":[]}}";

    wm.stubFor(WireMock.get(getSchema())
        .willReturn(responseDefinition().withStatus(SC_OK).withBody(response)
            .withHeader(CONTENT_TYPE, APPLICATION_JSON)
        )
    );
  }

  private static void stubUnpublication() {
    PublisherSuccessResult result = new PublisherSuccessResult(123456L);
    wm.stubFor(
        WireMock.delete(getPublicationPath(UnpublishCommandTest.CHANNEL, UnpublishCommandTest.KEY))
            .willReturn(responseDefinition().withStatus(SC_ACCEPTED).withBody(Json.write(result))
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
