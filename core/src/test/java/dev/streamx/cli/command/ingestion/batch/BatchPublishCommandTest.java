package dev.streamx.cli.command.ingestion.batch;

import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.github.tomakehurst.wiremock.matching.ContainsPattern;
import dev.streamx.cli.command.ingestion.AuthorizedProfile;
import dev.streamx.cli.command.ingestion.BaseIngestionCommandTest;
import dev.streamx.cli.command.ingestion.UnauthorizedProfile;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainLauncher;
import io.quarkus.test.junit.main.QuarkusMainTest;
import org.jose4j.base64url.Base64;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class BatchPublishCommandTest extends BaseIngestionCommandTest {

  @Nested
  @QuarkusMainTest
  @TestProfile(UnauthorizedProfile.class)
  class UnauthorizedTest {

    @Test
    public void shouldRejectInvalidDataJson(QuarkusMainLauncher launcher) {
      // when
      LaunchResult result = launcher.launch(
          BatchCommand.COMMAND_NAME, "--ingestion-url=" + getIngestionUrl(),
          "target/test-classes/dev/streamx/cli/command/ingestion/batch/invalid-json"
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
          BatchCommand.COMMAND_NAME, "--ingestion-url=" + getIngestionUrl(),
          "target/test-classes/dev/streamx/cli/command/ingestion/batch/valid/publish"
      );

      // then
      expectSuccess(result);
      wm.verify(postRequestedFor(urlEqualTo(PUBLICATION_PATH))
          .withRequestBody(new CloudEventJsonMatcher("""
              {
                "specversion" : "1.0",
                "id" : "75a90fb7-327e-4bee-96a1-3e4224a1e71d",
                "source" : "source",
                "type" : "page_publish",
                "datacontenttype" : "application/json",
                "subject" : "publish/index.html",
                "time" : "2025-12-23T10:28:23.435253Z",
                "data" : {
                  "content" : "%s",
                  "type" : "page/sub-page"
                }
              }
              """.formatted(Base64.encode("<h1>Hello World!</h1>".getBytes(UTF_8)))))
          .withoutHeader("Authorization"));
      wm.verify(1, postRequestedFor(urlEqualTo(PUBLICATION_PATH)));
    }

    @Test
    public void shouldBatchPublishValidDirectoryWithJsons(QuarkusMainLauncher launcher) {
      // when
      LaunchResult result = launcher.launch(
          BatchCommand.COMMAND_NAME, "--ingestion-url=" + getIngestionUrl(),
          "target/test-classes/dev/streamx/cli/command/ingestion/batch/valid-json"
      );

      // then
      expectSuccess(result);
      wm.verify(postRequestedFor(urlEqualTo(PUBLICATION_PATH))
          .withRequestBody(new CloudEventJsonMatcher("""
              {
                "specversion" : "1.0",
                "id" : "b0db1b2f-4069-4234-a968-9474f96ede9e",
                "source" : "source",
                "type" : "page_publish",
                "datacontenttype" : "application/json",
                "subject" : "content.json",
                "time" : "2025-12-23T11:00:17.710136Z",
                "data" : {
                  "object" : {
                    "content" : "<h1>Hello world!</h1>"
                  },
                  "fixed-property" : true,
                  "list" : [ {
                    "content" : "<h1>Hello world!</h1>"
                  } ]
                }
              }
              """))
          .withoutHeader("Authorization"));

      wm.verify(1, postRequestedFor(urlEqualTo(PUBLICATION_PATH)));
    }

    @Test
    public void shouldRejectWrongIngestionUrl(QuarkusMainLauncher launcher) {
      // when
      String ingestionServiceUrl = "http://aaa.bbb.ccc";
      LaunchResult result = launcher.launch(
          BatchCommand.COMMAND_NAME,
          "--ingestion-url=" + ingestionServiceUrl,
          "target/test-classes/dev/streamx/cli/command/ingestion/batch/valid/publish");

      // then
      expectError(result,
          "Error performing batch publication while processing 'target/test-classes/"
          + "dev/streamx/cli/command/ingestion/batch/valid/publish/index.html' file.\n"
          + "\n"
          + "Details:\n"
          + "Ingestion REST error: unknown host\n"
          + "\n"
          + "Full logs can be found in quarkus.log"
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
      LaunchResult result = launcher.launch(BatchCommand.COMMAND_NAME,
          "--ingestion-url=" + getIngestionUrl(),
          "target/test-classes/dev/streamx/cli/command/ingestion/batch/valid/publish");

      // then
      expectSuccess(result);
      wm.verify(postRequestedFor(urlEqualTo(PUBLICATION_PATH))
          .withHeader("Authorization", new ContainsPattern(AuthorizedProfile.AUTH_TOKEN)));
    }
  }
}
