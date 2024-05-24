package dev.streamx.cli;


import dev.streamx.cli.test.tools.ResourcePathResolver;
import dev.streamx.cli.test.tools.terminal.TerminalCommandRunner;
import dev.streamx.cli.test.tools.validators.HttpValidator;
import dev.streamx.cli.test.tools.validators.ProcessValidator;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class StreamxCliTestsIT {

  @ConfigProperty(name = "web.delivery.port.url", defaultValue = "http://localhost:8081/")
  String webDeliveryPortUrl;

  @Inject
  @Named("StreamxCommandRunner")
  TerminalCommandRunner terminalCommandRunner;

  @Inject
  ProcessValidator processValidator;

  @Inject
  ResourcePathResolver resourcePathResolver;

  @Inject
  HttpValidator httpValidator;

  @BeforeAll
  public void setup() {
    streamxCommand(
        "run -f " + filePath("streamx-mesh.yml"),
        "STREAMX IS READY!",
        60);
  }

  private String filePath(String fileName) {
    return resourcePathResolver.absolutePath(fileName);
  }

  private void streamxCommand(String command, String expectedOutput, long timeoutInS) {
    Process p = terminalCommandRunner.run(command);
    processValidator.validateOutput(p, expectedOutput, timeoutInS * 1000);
  }

  @Test
  public void shouldPublishPageFromJsonFile() {
    streamxCommand(
        "publish pages third_param_page.html " + filePath("payload.json"),
        "Registered",
        2);

    validateStreamxPage("third_param_page.html", 200, "third_param_page");

    streamxCommand(
        "unpublish pages third_param_page.html",
        "Registered unpublish event on",
        2);

    validateStreamxPage("third_param_page.html", 404, "");
  }

  @Test
  public void shouldPublishPageUsingInlinedJson() {
    streamxCommand(
        "publish pages exact_param_page.html -j '{\"content\":{\"bytes\":\"exact_param_page\"}}'",
        "Registered",
        2);

    validateStreamxPage("exact_param_page.html", 200, "exact_param_page");

    streamxCommand(
        "unpublish pages exact_param_page.html",
        "Registered unpublish event on",
        2);

    validateStreamxPage("exact_param_page.html", 404, "");
  }

  @Test
  public void shouldPublishPageUsingJsonPathParam() {
    streamxCommand(
        "publish pages json_path_exact_param_page.html -s content.bytes='Json exact page'",
        "Registered",
        2);

    validateStreamxPage("json_path_exact_param_page.html", 200, "Json exact page");

    streamxCommand(
        "unpublish pages json_path_exact_param_page.html",
        "Registered unpublish event on",
        2);

    validateStreamxPage("json_path_exact_param_page.html", 404, "");
  }

  @Test
  public void shouldPublishPageUsingFileParamPage() {
    streamxCommand(
        "publish pages file_param_page.html -j file://" + filePath("file_param_page.json"),
        "Registered",
        2);

    validateStreamxPage("file_param_page.html", 200, "file_param_page");

    streamxCommand(
        "unpublish pages file_param_page.html",
        "Registered unpublish event on",
        2);

    validateStreamxPage("file_param_page.html", 404, "");
  }

  @Test
  public void shouldPublishPageUsingJsonPathFileParamPage() {
    streamxCommand(
        "publish pages json_path_file_param_page.html -s content.bytes=file://"
            + filePath("json_path_file_param_page.json"),
        "Registered",
        2);

    validateStreamxPage("json_path_file_param_page.html", 200, "json_path_file_param_page");

    streamxCommand(
        "unpublish pages json_path_file_param_page.html",
        "Registered unpublish event on",
        2);

    validateStreamxPage("json_path_file_param_page.html", 404, "");
  }

  private void validateStreamxPage(String resourcePath, int expectedStatusCode,
      String expectedBody) {
    httpValidator.validate(webDeliveryPortUrl + resourcePath, expectedStatusCode, expectedBody, 2);
  }
}
