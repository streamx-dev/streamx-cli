package dev.streamx.cli.ingestion;

import static org.assertj.core.api.Assertions.assertThat;

import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainLauncher;
import io.quarkus.test.junit.main.QuarkusMainTest;
import org.junit.jupiter.api.Test;

@QuarkusMainTest
public class IngestionArgumentsValidationTest {

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
    assertThat(result.exitCode()).isNotZero();
    assertThat(result.getErrorOutput()).isEqualTo(
        "Publication endpoint URI: hattetepe:///in valid/publications/v1 is malformed. "
            + "Illegal character in path");
  }

  @Test
  public void shouldRejectChannellessIngestion(QuarkusMainLauncher launcher) {
    // when
    LaunchResult result = launcher.launch("unpublish");

    // then
    assertThat(result.exitCode()).isNotZero();
    assertThat(result.getErrorOutput()).contains("Missing required argument", "<channel>");
  }

  @Test
  public void shouldRejectKeylessIngestion(QuarkusMainLauncher launcher) {
    // when
    LaunchResult result = launcher.launch("unpublish", "channel");

    // then
    assertThat(result.exitCode()).isNotZero();
    assertThat(result.getErrorOutput()).contains("Missing required argument", "<key>");
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
    assertThat(result.exitCode()).isNotZero();
    assertThat(result.getErrorOutput()).isEqualTo(
        "Publication endpoint URI: hattetepe:///invalid/publications/v1 is malformed. "
            + "URI without host is not supported.");
  }
}
