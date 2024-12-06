package dev.streamx.cli.command.ingestion;

import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainLauncher;
import io.quarkus.test.junit.main.QuarkusMainTest;
import org.junit.jupiter.api.Test;

@QuarkusMainTest
public class IngestionArgumentsValidationTest extends BaseIngestionCommandTest {

  private static final String CHANNEL = "pages";
  private static final String KEY = "index.html";

  @Test
  public void shouldRejectIllegalIngestionUrl(QuarkusMainLauncher launcher) {
    // given
    String invalidIngestionUrl = "hattetepe:///in valid";

    // when
    LaunchResult result = launcher.launch("unpublish",
        "--ingestion-url=" + invalidIngestionUrl,
        CHANNEL, KEY);

    // then
    expectError(result,
        "Endpoint URI: hattetepe:///in valid/ingestion/v1 is malformed. "
        + "Illegal character in path");
  }

  @Test
  public void shouldRejectChannellessIngestion(QuarkusMainLauncher launcher) {
    // when
    LaunchResult result = launcher.launch("unpublish");

    // then
    expectError(result, """
        Error: Missing required argument(s): (<channel> <key>)
        Usage: streamx unpublish [-hV] [[--ingestion-url=<restIngestionServiceUrl>]]
                                 (<channel> <key>)

        Try 'streamx unpublish --help' for more information.""");
  }

  @Test
  public void shouldRejectIngestionWithMissingParameter(QuarkusMainLauncher launcher) {
    // when
    LaunchResult result = launcher.launch("publish", "-s");

    // then
    expectError(result, """
        Missing required parameter for option '--string-fragment' (<string>)
        Usage: streamx publish [-hV] [[--ingestion-url=<restIngestionServiceUrl>]]
                               (<channel> <key> [payloadFile]) [[[-s=<string> |
                               -b=<binary> | -j=<json>]]...]

        Try 'streamx publish --help' for more information.""");
  }

  @Test
  public void shouldRejectKeylessIngestion(QuarkusMainLauncher launcher) {
    // when
    LaunchResult result = launcher.launch("unpublish", "channel");

    // then
    expectError(result, """
        Error: Missing required argument(s): <key>
        Usage: streamx unpublish [-hV] [[--ingestion-url=<restIngestionServiceUrl>]]
                                 (<channel> <key>)

        Try 'streamx unpublish --help' for more information.""");
  }

  @Test
  public void shouldRejectInvalidHostInIngestionUrl(QuarkusMainLauncher launcher) {
    // given
    String invalidIngestionUrl = "hattetepe:///invalid";

    // when
    LaunchResult result = launcher.launch("unpublish",
        "--ingestion-url=" + invalidIngestionUrl,
        CHANNEL, KEY);

    // then
    expectError(result,
        "Endpoint URI: hattetepe:///invalid/ingestion/v1 is malformed. "
        + "URI without host is not supported.");
  }

  @Override
  protected void initializeWiremock() {
    // no mock responses to configure for this test
  }
}
