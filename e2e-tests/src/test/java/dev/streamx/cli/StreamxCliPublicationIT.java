package dev.streamx.cli;


import static dev.streamx.cli.test.tools.ResourcePathResolver.absolutePath;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import dev.streamx.cli.test.tools.terminal.TerminalCommandRunner;
import dev.streamx.cli.test.tools.terminal.process.ShellProcess;
import dev.streamx.cli.test.tools.validators.HttpValidator;
import dev.streamx.cli.test.tools.validators.ProcessOutputValidator;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.List;
import java.util.stream.Stream;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class StreamxCliPublicationIT {

  private static final int CLI_SHORT_TIMEOUT_IN_SEC = 5;
  @ConfigProperty(name = "streamx.cli.e2e.web.delivery.url", defaultValue = "http://localhost:8087/")
  String webDeliveryPortUrl;
  @ConfigProperty(name = "streamx.cli.e2e.nginx.url", defaultValue = "http://localhost:8089/overridden/")
  String nginxPortUrl;

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
        "--accept-license run -f " + absolutePath("mesh.yaml"),
        "STREAMX IS READY!",
        setupTimeoutInSec);
  }

  private void runStreamxCommand(String command, String expectedOutput, long timeoutInS) {
    ShellProcess p = terminalCommandRunner.run(command);
    processOutputValidator.validate(p.getCurrentOutputLines(), expectedOutput, timeoutInS);
  }


  @ParameterizedTest
  @MethodSource("testCases")
  public void shouldTestPublishAndUnpublishPageOnStreamx(
      String pageName,
      String commandContentPart,
      String expectedPageContent
  ) {

    runStreamxCommand(
        "--accept-license publish pages " + pageName + " " +  commandContentPart,
        "Registered data publication on",
        CLI_SHORT_TIMEOUT_IN_SEC);

    validateStreamxPage(pageName, 200, expectedPageContent);

    runStreamxCommand(
        "--accept-license unpublish pages " + pageName,
        "Registered unpublish trigger on",
        CLI_SHORT_TIMEOUT_IN_SEC);

    validateStreamxPage(pageName, 404, "");
  }

  static Stream<Arguments> testCases() {
    return Stream.of(
        arguments(
            "third_param_page.html",
            absolutePath("payload.json"),
            "third_param_page"
        ),
        arguments(
            "exact_param_page.html",
            "-j '{\"content\":{\"bytes\":\"exact_param_page\"}}'",
            "exact_param_page"
        ),
        arguments(
            "json_path_exact_param_page.html",
            "-s content.bytes='Json exact page'",
            "Json exact page"
        ),
        arguments(
            "file_param_page.html",
            "-j file://" + absolutePath("file_param_page.json"),
            "file_param_page"
        ),
        arguments(
            "json_path_file_param_page.html",
            "-s content.bytes=file://" + absolutePath("json_path_file_param_page.txt"),
            "json_path_file_param_page"
        ),
        arguments(
            "json_path_file_param_page.html",
            "-b content.bytes=file://" + absolutePath("json_path_file_param_page.txt"),
            "json_path_file_param_page"
        )
    );
  }

  private void validateStreamxPage(String resourcePath, int expectedStatusCode,
      String expectedBody) {
    List.of(webDeliveryPortUrl, nginxPortUrl).forEach(
        url -> httpValidator.validate(url + resourcePath, expectedStatusCode, expectedBody,
              CLI_SHORT_TIMEOUT_IN_SEC)
    );
  }
}
