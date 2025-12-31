package dev.streamx.cli;

import static dev.streamx.cli.test.tools.ResourcePathResolver.absolutePath;

import dev.streamx.cli.test.tools.terminal.TerminalCommandRunner;
import dev.streamx.cli.test.tools.terminal.process.ShellProcess;
import dev.streamx.cli.test.tools.validators.HttpValidator;
import dev.streamx.cli.test.tools.validators.ProcessOutputValidator;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jetbrains.annotations.NotNull;
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
  String webDeliveryUrl;

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
    processOutputValidator.validate(p, p.getCurrentOutputLines(), expectedOutput, timeout);
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

    validateStreamxPage("hello.html", "<b>Hello World!</b>");

    runStreamxIngestionCommand(
        "stream_v2",
        "src/test/resources/stream/page-unpublish-event.json",
        "Sent com.streamx.blueprints.page.unpublished.v1 event using stream with key 'hello.html'"
    );

    validateStreamxPageNotAvailable("hello.html");
  }

  @Test
  public void shouldPublishAndUnpublishPageUsingBatchOperation() {
    runStreamxIngestionCommand(
        "batch_v2",
        "src/test/resources/batch/publish/page",
        "Sent com.streamx.blueprints.page.published.v1 event using batch with key 'index.html'"
    );

    validateStreamxPage("index.html", "<h1>Hello World!</h1>");

    runStreamxIngestionCommand(
        "batch_v2",
        "src/test/resources/batch/unpublish/page",
        "Sent com.streamx.blueprints.page.unpublished.v1 event using batch with key 'index.html'"
    );

    validateStreamxPageNotAvailable("index.html");
  }

  @Test
  public void shouldPublishAndUnpublishAssetUsingBatchOperation() throws IOException {
    runStreamxIngestionCommand(
        "batch_v2",
        "src/test/resources/batch/publish/asset",
        "Sent com.streamx.blueprints.asset.published.v1 event using batch with key 'ds.png'"
    );

    validateStreamxPage("ds.png",
        Files.readAllBytes(Path.of("src/test/resources/batch/publish/asset/ds.png")));

    runStreamxIngestionCommand(
        "batch_v2",
        "src/test/resources/batch/unpublish/asset",
        "Sent com.streamx.blueprints.asset.unpublished.v1 event using batch with key 'ds.png'"
    );

    validateStreamxPageNotAvailable("ds.png");
  }

  private void validateStreamxPage(String resourcePath, String expectedBody) {
    httpValidator.validate(url(resourcePath), 200, expectedBody, CLI_TIMEOUT);
  }

  private void validateStreamxPage(String resourcePath, byte[] expectedBody) {
    httpValidator.validate(url(resourcePath), 200, expectedBody, CLI_TIMEOUT);
  }

  private void validateStreamxPageNotAvailable(String resourcePath) {
    httpValidator.validate(url(resourcePath), 404, "", CLI_TIMEOUT);
  }

  private String url(String resourcePath) {
    return webDeliveryUrl + resourcePath;
  }
}
