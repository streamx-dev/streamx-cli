package dev.streamx.cli.ingestion.publish;

import static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.responseDefinition;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.common.ContentTypes.APPLICATION_JSON;
import static com.github.tomakehurst.wiremock.common.ContentTypes.CONTENT_TYPE;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static dev.streamx.clients.ingestion.StreamxClient.PUBLICATIONS_ENDPOINT_PATH_V1;
import static org.apache.hc.core5.http.HttpStatus.SC_ACCEPTED;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.matching.ContainsPattern;
import dev.streamx.clients.ingestion.publisher.PublisherSuccessResult;
import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainLauncher;
import io.quarkus.test.junit.main.QuarkusMainTest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

@QuarkusMainTest
public class PublishPayloadCommandTest {

  private static final String CHANNEL = "pages";
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
  public void shouldRejectInvalidJsonPath(QuarkusMainLauncher launcher) {
    // when
    LaunchResult result = launcher.launch("publish",
        "--ingestion-url=" + getIngestionUrl(),
        "-j", DATA,
        "-s", "content.b[]ytes=<h1>Hello changed value!</h1>",
        CHANNEL, KEY);

    // then
    assertThat(result.exitCode()).isNotZero();
    assertThat(result.getErrorOutput()).contains(
        "Could not find valid JSONPath expression in given option.");
  }

  @Test
  public void shouldRejectNonExistingFile(QuarkusMainLauncher launcher) {
    // when
    LaunchResult result = launcher.launch("publish",
        "--ingestion-url=" + getIngestionUrl(),
        "-s", "content.bytes=file://nana",
        CHANNEL, KEY);

    // then
    assertThat(result.exitCode()).isNotZero();
    assertThat(result.getErrorOutput()).contains("File does not exist.");
  }

  @Test
  public void shouldRejectInvalidFile(QuarkusMainLauncher launcher) {
    // given
    String corruptedPathArg =
        "file://target/test-classes/dev/streamx/cli/publish/payload/invalid-payload.json";

    // when
    LaunchResult result = launcher.launch("publish",
        "--ingestion-url=" + getIngestionUrl(),
        "-j", "content.bytes=" + corruptedPathArg,
        CHANNEL, KEY);

    // then
    assertThat(result.exitCode()).isNotZero();
    assertThat(result.getErrorOutput()).contains("Replacement is not recognised as valid JSON.");
  }

  @Test
  public void shouldPublishReplacedJsonPath(QuarkusMainLauncher launcher) {
    // when
    LaunchResult result = launcher.launch("publish",
        "--ingestion-url=" + getIngestionUrl(),
        "-s", "content.bytes=<h1>Hello changed value!</h1>",
        CHANNEL, KEY);

    // then
    assertThat(result.exitCode()).isZero();
    wm.verify(putRequestedFor(urlEqualTo(
        getPublicationPath(PublishPayloadCommandTest.CHANNEL, PublishPayloadCommandTest.KEY)))
        .withRequestBody(
            equalToJson("{\"content\": {\"bytes\": \"<h1>Hello changed value!</h1>\"}}")));
  }

  @Test
  public void shouldPublishReplacedWithObjectJsonPath(QuarkusMainLauncher launcher) {
    // when
    LaunchResult result = launcher.launch("publish",
        "--ingestion-url=" + getIngestionUrl(),
        "-j", DATA,
        "-j", "content={'bytes':'<h1>Hello changed value!</h1>'}",
        CHANNEL, KEY);

    // then
    assertThat(result.exitCode()).isZero();
    wm.verify(putRequestedFor(urlEqualTo(
        getPublicationPath(PublishPayloadCommandTest.CHANNEL, PublishPayloadCommandTest.KEY)))
        .withRequestBody(
            equalToJson("{\"content\": {\"bytes\": \"<h1>Hello changed value!</h1>\"}}")));
  }

  @Test
  public void shouldPublishReplacedFromFile(QuarkusMainLauncher launcher) {
    // given
    String arg = "file://target/test-classes/dev/streamx/cli/publish/payload/payload.json";

    // when
    LaunchResult result = launcher.launch("publish",
        "--ingestion-url=" + getIngestionUrl(),
        "-j", "content.bytes=" + arg,
        CHANNEL, KEY);

    // then
    assertThat(result.exitCode()).isZero();
    wm.verify(putRequestedFor(urlEqualTo(
        getPublicationPath(PublishPayloadCommandTest.CHANNEL, PublishPayloadCommandTest.KEY)))
        .withRequestBody(equalToJson("""
            {"content": {"bytes": {"nana": "lele"}}}""")));
  }

  @Test
  public void shouldPublishReplacedWithNull(QuarkusMainLauncher launcher) {
    // when
    LaunchResult result = launcher.launch("publish",
        "--ingestion-url=" + getIngestionUrl(),
        "-j", DATA,
        "-j", "content.bytes=",
        CHANNEL, KEY);

    // then
    assertThat(result.exitCode()).isZero();
    wm.verify(putRequestedFor(urlEqualTo(
        getPublicationPath(PublishPayloadCommandTest.CHANNEL, PublishPayloadCommandTest.KEY)))
        .withRequestBody(equalToJson("""
            {"content": {"bytes": null}}""")));
  }

  @Test
  public void shouldPublishTwiceReplacedJsonPath(QuarkusMainLauncher launcher) {
    // when
    LaunchResult result = launcher.launch("publish",
        "--ingestion-url=" + getIngestionUrl(),
        "-s", "content.bytes=<h1>Hello changed value!</h1>",
        "-s", "$..bytes=bytes",
        CHANNEL, KEY);

    // then
    assertThat(result.exitCode()).isZero();
    wm.verify(putRequestedFor(urlEqualTo(
        getPublicationPath(PublishPayloadCommandTest.CHANNEL, PublishPayloadCommandTest.KEY)))
        .withRequestBody(equalToJson("{\"content\": {\"bytes\": \"bytes\"}}")));
  }

  @Test
  public void shouldPublishReplacedJsonPathWithStringValue(QuarkusMainLauncher launcher) {
    // when
    LaunchResult result = launcher.launch("publish",
        "--ingestion-url=" + getIngestionUrl(),
        "-s", "content.bytes=<h1>Hello changed value!</h1>",
        CHANNEL, KEY);

    // then
    assertThat(result.exitCode()).isZero();
    wm.verify(putRequestedFor(urlEqualTo(
        getPublicationPath(PublishPayloadCommandTest.CHANNEL, PublishPayloadCommandTest.KEY)))
        .withRequestBody(
            equalToJson("{\"content\": {\"bytes\": \"<h1>Hello changed value!</h1>\"}}")));
  }

  @Test
  public void shouldPublishReplacedJsonPathWithStringValueFromFile(QuarkusMainLauncher launcher) {
    // given
    String arg = "file://target/test-classes/dev/streamx/cli/publish/payload/raw-text.txt";

    // when
    LaunchResult result = launcher.launch("publish",
        "--ingestion-url=" + getIngestionUrl(),
        "-s", "content.bytes=" + arg,
        CHANNEL, KEY);

    // then
    assertThat(result.exitCode()).isZero();
    wm.verify(putRequestedFor(urlEqualTo(
        getPublicationPath(PublishPayloadCommandTest.CHANNEL, PublishPayloadCommandTest.KEY)))
        .withRequestBody(
            equalToJson("{\"content\": {\"bytes\": \"<h1>This works ąćpretty well...</h1>\"}}")));
  }

  @Test
  public void shouldPublishReplacedJsonPathWithBinaryValue(QuarkusMainLauncher launcher) {
    // given
    String arg = "file://target/test-classes/dev/streamx/cli/publish/payload/example-image.png";

    // when
    LaunchResult result = launcher.launch("publish",
        "--ingestion-url=" + getIngestionUrl(),
        "-s", "content.bytes=" + arg,
        CHANNEL, KEY);

    // then
    assertThat(result.exitCode()).isZero();
    wm.verify(putRequestedFor(urlEqualTo(
        getPublicationPath(PublishPayloadCommandTest.CHANNEL, PublishPayloadCommandTest.KEY)))
        .withRequestBody(matchingJsonPath("content.bytes", new ContainsPattern("PNG"))));
  }

  private static void initializeWiremock() {
    stubPublication();
  }

  private static void stubPublication() {
    PublisherSuccessResult result = new PublisherSuccessResult(123456L);
    wm.stubFor(
        put(getPublicationPath(PublishPayloadCommandTest.CHANNEL, PublishPayloadCommandTest.KEY))
            .willReturn(responseDefinition().withStatus(SC_ACCEPTED).withBody(Json.write(result))
                .withHeader(CONTENT_TYPE, APPLICATION_JSON)));
  }

  @NotNull
  private static String getIngestionUrl() {
    return "http://localhost:" + wm.getPort();
  }

  private static String getPublicationPath(String channel, String key) {
    return PUBLICATIONS_ENDPOINT_PATH_V1 + "/" + channel + "/" + key;
  }
}
