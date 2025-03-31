package dev.streamx.cli.command.ingestion.publish;

import static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.responseDefinition;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.common.ContentTypes.APPLICATION_JSON;
import static com.github.tomakehurst.wiremock.common.ContentTypes.CONTENT_TYPE;
import static org.apache.hc.core5.http.HttpStatus.SC_ACCEPTED;

import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.matching.ContainsPattern;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import dev.streamx.cli.command.ingestion.BaseIngestionCommandTest;
import dev.streamx.clients.ingestion.publisher.IngestionResult;
import dev.streamx.clients.ingestion.publisher.SuccessResult;
import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainLauncher;
import io.quarkus.test.junit.main.QuarkusMainTest;
import org.junit.jupiter.api.Test;

@QuarkusMainTest
public class PublishPayloadCommandTest extends BaseIngestionCommandTest {

  private static final String CHANNEL = "pages";
  private static final String KEY = "index.html";
  private static final String DATA = """
      {"content": {"bytes": "<h1>Hello World!</h1>"}}""";
  private static final String TEST_RESOURCES = "file://target/test-classes/";

  @Test
  public void shouldRejectInvalidJsonPath(QuarkusMainLauncher launcher) {
    // when
    LaunchResult result = launcher.launch("publish",
        "--ingestion-url=" + getIngestionUrl(),
        "-j", DATA,
        "-s", "content.b[]ytes=<h1>Hello changed value!</h1>",
        CHANNEL, KEY);

    // then
    expectError(result, """
        Could not find valid JSONPath expression in given option.
                
        Option: content.b[]ytes=<h1>Hello changed value!</h1>
                
        Verify:
         * if given JSONPath expression is valid \
        (according to https://github.com/json-path/JsonPath docs),
         * if '=' is present in option""");
  }

  @Test
  public void shouldRejectNonExistingFile(QuarkusMainLauncher launcher) {
    // when
    LaunchResult result = launcher.launch("publish",
        "--ingestion-url=" + getIngestionUrl(),
        "-s", "content.bytes=file://nana",
        CHANNEL, KEY);

    // then
    expectError(result, """
        File does not exist.
        Path: nana""");
  }

  @Test
  public void shouldRejectInvalidFile(QuarkusMainLauncher launcher) {
    // given
    String corruptedPathArg =
        TEST_RESOURCES + "dev/streamx/cli/command/ingestion/publish/payload/invalid-payload.json";

    // when
    LaunchResult result = launcher.launch("publish",
        "--ingestion-url=" + getIngestionUrl(),
        "-j", "content.bytes=" + corruptedPathArg,
        CHANNEL, KEY);

    // then
    expectError(result, """
        Replacement is not recognised as valid JSON.
                
        Supplied JSONPath expression:
        $['content']['bytes']
        Supplied replacement:
        file://target/test-classes/dev/streamx/cli/command/ingestion/publish/payload/\
        invalid-payload.json
                
        Make sure that:
         * you need a JSON node as replacement
            (alternatively use '-s' to specify raw text replacement
            or use '-b' to specify is binary replacement),
         * it's valid JSON,
         * object property names are properly single-quoted (') or double-quoted ("),
         * strings are properly single-quoted (') or double-quoted (")
                
        Details: Unexpected end-of-input: expected close marker for Object (start marker\
         at [Source: REDACTED (`StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION` disabled);\
         line: 1, column: 1])
         at [Source: REDACTED (`StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION` disabled);\
         line: 3, column: 1]""");
  }

  @Test
  public void shouldPublishReplacedJsonPath(QuarkusMainLauncher launcher) {
    // when
    LaunchResult result = launcher.launch("publish",
        "--ingestion-url=" + getIngestionUrl(),
        "-s", "content.bytes=<h1>Hello changed value!</h1>",
        CHANNEL, KEY);

    // then
    expectSuccess(result);
    wm.verify(postRequestedFor(urlEqualTo(
        getPublicationPath(CHANNEL)))
        .withRequestBody(
            equalToJson(
                buildResponseWith(
                    "{\"content\": {\"bytes\": \"<h1>Hello changed value!</h1>\"}}"))));
  }

  @Test
  public void shouldPublishWithProperties(QuarkusMainLauncher launcher) {
    // when
    LaunchResult result = launcher.launch("publish",
        "--ingestion-url=" + getIngestionUrl(),
        "-s", "content.bytes=<h1>Hello changed value!</h1>",
        "-p", "sx:type=type/subtype",
        CHANNEL, KEY);

    // then
    expectSuccess(result);
    wm.verify(postRequestedFor(urlEqualTo(
        getPublicationPath(CHANNEL)))
        .withRequestBody(
            equalToJson(
                buildResponseWith(
                    "{\"content\": {\"bytes\": \"<h1>Hello changed value!</h1>\"}}",
                    "{ \"sx:type\": \"type/subtype\" }"))));
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
    expectSuccess(result);
    wm.verify(postRequestedFor(urlEqualTo(
        getPublicationPath(PublishPayloadCommandTest.CHANNEL)))
        .withRequestBody(
            equalToJson(
                buildResponseWith(
                    "{\"content\": {\"bytes\": \"<h1>Hello changed value!</h1>\"}}"))));
  }

  @Test
  public void shouldPublishReplacedFromFile(QuarkusMainLauncher launcher) {
    // given
    String arg = TEST_RESOURCES + "dev/streamx/cli/command/ingestion/publish/payload/payload.json";

    // when
    LaunchResult result = launcher.launch("publish",
        "--ingestion-url=" + getIngestionUrl(),
        "-j", "content.bytes=" + arg,
        CHANNEL, KEY);

    // then
    expectSuccess(result);
    wm.verify(postRequestedFor(urlEqualTo(
        getPublicationPath(CHANNEL)))
        .withRequestBody(equalToJson(buildResponseWith("""
            {"content": {"bytes": {"nana": "lele"}}}"""))));
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
    expectSuccess(result);
    wm.verify(postRequestedFor(urlEqualTo(
        getPublicationPath(CHANNEL)))
        .withRequestBody(equalToJson(buildResponseWith("""
            {"content": {"bytes": null}}"""))));
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
    expectSuccess(result);
    wm.verify(postRequestedFor(urlEqualTo(
        getPublicationPath(CHANNEL)))
        .withRequestBody(equalToJson(buildResponseWith("{\"content\": {\"bytes\": \"bytes\"}}"))));
  }

  @Test
  public void shouldPublishReplacedJsonPathWithStringValue(QuarkusMainLauncher launcher) {
    // when
    LaunchResult result = launcher.launch("publish",
        "--ingestion-url=" + getIngestionUrl(),
        "-s", "content.bytes=<h1>Hello changed value!</h1>",
        CHANNEL, KEY);

    // then
    expectSuccess(result);
    wm.verify(postRequestedFor(urlEqualTo(
        getPublicationPath(CHANNEL)))
        .withRequestBody(
            equalToJson(buildResponseWith(
                "{\"content\": {\"bytes\": \"<h1>Hello changed value!</h1>\"}}"))));
  }

  @Test
  public void shouldPublishReplacedJsonPathWithStringValueFromFile(QuarkusMainLauncher launcher) {
    // given
    String arg = TEST_RESOURCES + "dev/streamx/cli/command/ingestion/publish/payload/raw-text.txt";

    // when
    LaunchResult result = launcher.launch("publish",
        "--ingestion-url=" + getIngestionUrl(),
        "-s", "content.bytes=" + arg,
        CHANNEL, KEY);

    // then
    expectSuccess(result);
    wm.verify(postRequestedFor(urlEqualTo(
        getPublicationPath(CHANNEL)))
        .withRequestBody(
            equalToJson(buildResponseWith(
                "{\"content\": {\"bytes\": \"<h1>This works ąćpretty well...</h1>\"}}"))));
  }

  @Test
  public void shouldPublishReplacedJsonPathWithBinaryValue(QuarkusMainLauncher launcher) {
    // given
    String arg =
        TEST_RESOURCES + "dev/streamx/cli/command/ingestion/publish/payload/example-image.png";

    // when
    LaunchResult result = launcher.launch("publish",
        "--ingestion-url=" + getIngestionUrl(),
        "-s", "content.bytes=" + arg,
        CHANNEL, KEY);

    // then
    expectSuccess(result);
    StringValuePattern matchingPngFileContent = matchingJsonPath(
        "payload[\"dev.streamx.blueprints.data.Page\"].content.bytes",
        new ContainsPattern("PNG")
    );

    wm.verify(postRequestedFor(urlEqualTo(
        getPublicationPath(CHANNEL)))
        .withRequestBody(matchingPngFileContent));
  }

  private String buildResponseWith(String content) {
    return buildResponseWith(content, "{ }");
  }

  private String buildResponseWith(String content, String properties) {
    return
        """
            {
              "key" : "index.html",
              "action" : "publish",
              "eventTime" : null,
              "properties" : %s,
              "payload" : {
                "dev.streamx.blueprints.data.Page" : %s
              }
            }
            """.formatted(properties, content);
  }


  @Override
  protected void initializeWiremock() {
    var result = IngestionResult.of(new SuccessResult(123456L, KEY));
    wm.stubFor(
        post(getPublicationPath(CHANNEL))
            .willReturn(responseDefinition().withStatus(SC_ACCEPTED).withBody(Json.write(result))
                .withHeader(CONTENT_TYPE, APPLICATION_JSON)));

    setupMockChannelsSchemasResponse();
  }
}
