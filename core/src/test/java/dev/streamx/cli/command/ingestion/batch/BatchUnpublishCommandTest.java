package dev.streamx.cli.command.ingestion.batch;

import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import com.github.tomakehurst.wiremock.matching.ContainsPattern;
import dev.streamx.cli.command.ingestion.AuthorizedProfile;
import dev.streamx.cli.command.ingestion.BaseIngestionCommandTest;
import dev.streamx.cli.command.ingestion.UnauthorizedProfile;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainLauncher;
import io.quarkus.test.junit.main.QuarkusMainTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class BatchUnpublishCommandTest extends BaseIngestionCommandTest {

  @Nested
  @QuarkusMainTest
  @TestProfile(UnauthorizedProfile.class)
  class UnauthorizedTest {

    @Test
    public void shouldUnpublishUsingIngestionClient(QuarkusMainLauncher launcher) {
      // when
      LaunchResult result = launcher.launch(
          BatchCommand.COMMAND_NAME,
          "--ingestion-url=" + getIngestionUrl(),
          "target/test-classes/dev/streamx/cli/command/ingestion/batch/valid/unpublish");

      // then
      expectSuccess(result);
    }

    @Test
    public void shouldUnpublishUsingUnauthorizedIngestionClient(QuarkusMainLauncher launcher) {
      // when
      LaunchResult result = launcher.launch(
          BatchCommand.COMMAND_NAME,
          "--ingestion-url=" + getIngestionUrl(),
          "target/test-classes/dev/streamx/cli/command/ingestion/batch/valid/unpublish");

      // then
      expectSuccess(result);

      wm.verify(postRequestedFor(urlEqualTo(PUBLICATION_PATH))
          .withoutHeader("Authorization"));
    }
  }

  @Nested
  @QuarkusMainTest
  @TestProfile(AuthorizedProfile.class)
  class AuthorizedTest {

    @Test
    public void shouldUnpublishUsingAuthorizedIngestionClient(QuarkusMainLauncher launcher) {
      // when
      LaunchResult result = launcher.launch(
          BatchCommand.COMMAND_NAME,
          "--ingestion-url=" + getIngestionUrl(),
          "target/test-classes/dev/streamx/cli/command/ingestion/batch/valid/unpublish");

      // then
      expectSuccess(result);

      wm.verify(
          postRequestedFor(urlEqualTo(PUBLICATION_PATH))
              .withRequestBody(new CloudEventJsonMatcher("""
                  {
                    "specversion" : "1.0",
                    "id" : "81099e4d-a4ec-44e9-8d5c-8f177af8d22c",
                    "source" : "source",
                    "type" : "page_unpublish",
                    "datacontenttype":"application/json",
                    "subject" : "unpublish/index.html",
                    "time" : "2025-12-23T11:59:29.946127Z",
                    "data" : {
                      "type" : "page/sub-page"
                    }
                  }
              """))
              .withHeader("Authorization", new ContainsPattern(AuthorizedProfile.AUTH_TOKEN)));
    }
  }
}
