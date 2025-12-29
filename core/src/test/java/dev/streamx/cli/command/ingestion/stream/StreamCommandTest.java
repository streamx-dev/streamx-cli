package dev.streamx.cli.command.ingestion.stream;

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

@QuarkusMainTest
class StreamCommandTest extends BaseIngestionCommandTest {

  @Nested
  @QuarkusMainTest
  @TestProfile(UnauthorizedProfile.class)
  class UnauthorizedTest {

    @Test
    public void shouldPublishUsingIngestionClient(QuarkusMainLauncher launcher) {
      // when
      LaunchResult result = launcher.launch(StreamCommand.COMMAND_NAME,
          "--ingestion-url=" + getIngestionUrl(),
          "target/test-classes/dev/streamx/cli/command/ingestion/stream/valid-json.stream");

      // then
      expectSuccess(result);
      wm.verify(postRequestedFor(urlEqualTo(PUBLICATION_PATH))
          .withoutHeader("Authorization"));
    }

    @Test
    public void shouldRejectInvalidJson(QuarkusMainLauncher launcher) {
      // when
      LaunchResult result = launcher.launch(StreamCommand.COMMAND_NAME,
          "--ingestion-url=" + getIngestionUrl(),
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
      LaunchResult result = launcher.launch(StreamCommand.COMMAND_NAME,
          "--ingestion-url=" + getIngestionUrl(),
          "target/test-classes/dev/streamx/cli/command/ingestion/stream/illegal-json.stream");

      // then
      expectError(result, "Error performing stream publication using "
                          + "'target/test-classes/dev/streamx/cli/command/ingestion/"
                          + "stream/illegal-json.stream' file.\n"
                          + "\n"
                          + "Details:\n"
                          + "Invalid data: Missing mandatory specversion attribute\n"
                          + "\n"
                          + "Full logs can be found in quarkus.log");
    }

    @Test
    public void shouldRejectInvalidJsonSeparator(QuarkusMainLauncher launcher) {
      // when
      LaunchResult result = launcher.launch(StreamCommand.COMMAND_NAME,
          "--ingestion-url=" + getIngestionUrl(),
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
                          + "line: 8, column: 2]");
    }
  }

  @Nested
  @QuarkusMainTest
  @TestProfile(AuthorizedProfile.class)
  class AuthorizedTest {

    @Test
    public void shouldPublishAuthorizedUsing(QuarkusMainLauncher launcher) {
      // when
      LaunchResult result = launcher.launch(StreamCommand.COMMAND_NAME,
          "--ingestion-url=" + getIngestionUrl(),
          "target/test-classes/dev/streamx/cli/command/ingestion/stream/valid-json.stream");

      // then
      expectSuccess(result);
      wm.verify(postRequestedFor(urlEqualTo(PUBLICATION_PATH))
          .withHeader("Authorization", new ContainsPattern(AuthorizedProfile.AUTH_TOKEN)));
    }
  }
}
