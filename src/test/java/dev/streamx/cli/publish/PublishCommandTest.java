package dev.streamx.cli.publish;

import static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.responseDefinition;
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
import dev.streamx.clients.ingestion.publisher.PublisherSuccessResult;
import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainLauncher;
import io.quarkus.test.junit.main.QuarkusMainTest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

@QuarkusMainTest
public class PublishCommandTest {

  @RegisterExtension
  static WireMockExtension wm = WireMockExtension.newInstance()
      .options(wireMockConfig().dynamicPort()).configureStaticDsl(true).build();

  @Test
  public void shouldPublishUsingIngestionClient(QuarkusMainLauncher launcher) {
    // given
    String channel = "pages";
    String key = "index.html";

    stubSchemas();
    stubPublication(channel, key);

    // when
    LaunchResult result = launcher.launch("publish",
        "--ingestion-url=" + getIngestionUrl(),
        channel, key);

    // then
    assertThat(result.exitCode()).isZero();
  }

  @Test
  public void shouldRejectUnknownChannel(QuarkusMainLauncher launcher) {
    // given
    String channel = "channel";
    String key = "index.html";

    stubSchemas();
    stubPublication(channel, key);

    // when
    LaunchResult result = launcher.launch("publish",
        "--ingestion-url=" + getIngestionUrl(),
        channel, key);

    // then
    assertThat(result.getErrorOutput()).containsSubsequence("Channel", "not found");
    assertThat(result.exitCode()).isNotZero();
  }

  private static void stubSchemas() {
    String response = """
        {"pages":{"type":"record","name":"Page","namespace":"dev.streamx.blueprints.data","fields":[{"name":"content","type":["null","bytes"],"default":null}]},"assets":{"type":"record","name":"Asset","namespace":"dev.streamx.blueprints.data","fields":[{"name":"content","type":["null","bytes"],"default":null}]},"data":{"type":"record","name":"Data","namespace":"dev.streamx.blueprints.data","fields":[{"name":"content","type":["null","bytes"],"default":null}]},"templates":{"type":"record","name":"Template","namespace":"dev.streamx.blueprints.data","fields":[{"name":"content","type":["null","bytes"],"default":null}]},"web-resources":{"type":"record","name":"WebResource","namespace":"dev.streamx.blueprints.data","fields":[{"name":"content","type":["null","bytes"],"default":null}]}}""";

    wm.stubFor(WireMock.get(getSchema())
        .willReturn(responseDefinition().withStatus(SC_OK).withBody(response)
            .withHeader(CONTENT_TYPE, APPLICATION_JSON)
        )
    );
  }

  private static void stubPublication(String channel, String key) {
    PublisherSuccessResult result = new PublisherSuccessResult(123456L);
    wm.stubFor(WireMock.put(getPublicationPath(channel, key))
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
