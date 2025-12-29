package dev.streamx.cli;

import static dev.streamx.cli.test.tools.ResourcePathResolver.absolutePath;

import dev.streamx.cli.test.tools.terminal.TerminalCommandRunner;
import dev.streamx.cli.test.tools.terminal.process.ShellProcess;
import dev.streamx.cli.test.tools.validators.HttpValidator;
import dev.streamx.cli.test.tools.validators.ProcessOutputValidator;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.time.Duration;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.condition.EnabledIf;

@QuarkusTest
@EnabledIf("dev.streamx.cli.OsUtils#isDockerAvailable")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class StreamxCliPublicationIT {

  private static final Duration CLI_TIMEOUT = Duration.ofSeconds(10);
  @ConfigProperty(name = "streamx.cli.e2e.web.delivery.url", defaultValue = "http://localhost:8087/")
  String webDeliveryPortUrl;

  @ConfigProperty(name = "streamx.cli.e2e.setup.timeoutInSec", defaultValue = "60")
  int setupTimeoutInSec;

  @Inject
  @Named("StreamxCommandRunner")
  TerminalCommandRunner terminalCommandRunner;

  @Inject
  ProcessOutputValidator processOutputValidator;

  @Inject
  HttpValidator httpValidator;

  @BeforeAll
  public void setup() {
    runStreamxCommand(
        "--accept-license run_v2 -f " + absolutePath("mesh.yaml"),
        "STREAMX IS READY!",
        Duration.ofSeconds(setupTimeoutInSec));
  }

  private void runStreamxCommand(String command, String expectedOutput, Duration timeout) {
    ShellProcess p = terminalCommandRunner.run(command);
    processOutputValidator.validate(p.getCurrentOutputLines(), expectedOutput, timeout);
  }

  private void runStreamxIngestionCommand(String commandName, String path, String expectedOutput) {
    String command = "--accept-license %s %s".formatted(commandName, path);
    runStreamxCommand(command, expectedOutput, CLI_TIMEOUT);
  }

  @Test
  public void shouldPublishAndUnpublishPageUsingStreamOperation() {
    runStreamxIngestionCommand(
        "stream_v2",
        "src/test/resources/stream/page-publish-event.json",
        "Sent com.streamx.blueprints.page.published.v1 event using stream with key 'hello.html'"
    );

    validateStreamxPage("hello.html", 200, "<b>Hello World!</b>");

    runStreamxIngestionCommand(
        "stream_v2",
        "src/test/resources/stream/page-unpublish-event.json",
        "Sent com.streamx.blueprints.page.unpublished.v1 event using stream with key 'hello.html'"
    );

    validateStreamxPage("hello.html", 404, "");
  }

  @Test
  public void shouldPublishAndUnpublishPageUsingBatchOperation() {
    runStreamxIngestionCommand(
        "batch_v2",
        "src/test/resources/batch/publish",
        "Sent com.streamx.blueprints.page.published.v1 event using batch with key 'index.html'"
    );

    validateStreamxPage("index.html", 200, "<h1>Hello World!</h1>");

    runStreamxIngestionCommand(
        "batch_v2",
        "src/test/resources/batch/unpublish",
        "Sent com.streamx.blueprints.page.unpublished.v1 event using batch with key 'index.html'"
    );

    validateStreamxPage("index.html", 404, "");
  }

  private void validateStreamxPage(String resourcePath, int expectedStatusCode,
      String expectedBody) {
    String url = webDeliveryPortUrl + resourcePath;
    httpValidator.validate(url, expectedStatusCode, expectedBody, CLI_TIMEOUT);
  }
}
