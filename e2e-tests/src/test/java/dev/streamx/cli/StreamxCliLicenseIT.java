package dev.streamx.cli;


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
import java.nio.file.Paths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class StreamxCliLicenseIT {

  private static final int CLI_SHORT_TIMEOUT_IN_SEC = 5;

  @Inject
  @Named("StreamxCommandRunner")
  TerminalCommandRunner terminalCommandRunner;

  @Inject
  ProcessOutputValidator processOutputValidator;

  @Inject
  HttpValidator httpValidator;

  @BeforeEach
  public void cleanUpBeforeTests() {
    resetLicenseAcceptance();
  }

  @Test
  public void shouldNotAcceptingLicensePreventFromUsing()  {
    ShellProcess firstRunProcess = runStreamxCommand();

    assertIfAcceptLicenseQuestionPresent(firstRunProcess);

    declineLicense(firstRunProcess);

    assertIfAcceptanceLicenseErrorPresent(firstRunProcess);

    ShellProcess secondRunProcess = runStreamxCommand();

    assertIfAcceptLicenseQuestionPresent(secondRunProcess);

    declineLicense(secondRunProcess);

    assertIfAcceptanceLicenseErrorPresent(secondRunProcess);
  }

  @Test
  public void shouldLicenseLinkBePresentAndAccessible()  {
    ShellProcess process = runStreamxCommand();

    assertIfAcceptLicenseQuestionPresent(process);

    assertIfLicenseIsAccessible(process);

    declineLicense(process);
  }

  @Test
  public void shouldAcceptingLicenseAllowUsingWithoutMoreQuestions()  {
    ShellProcess firstRunProcess = runStreamxCommand();

    assertIfAcceptLicenseQuestionPresent(firstRunProcess);

    acceptLicense(firstRunProcess);

    assertIfStreamxCommandWork(firstRunProcess);

    ShellProcess secondRunProcess = runStreamxCommand();

    assertIfStreamxCommandWork(secondRunProcess);
  }

  private void assertIfLicenseIsAccessible(ShellProcess process) {
    String url = processOutputValidator.validateContainsUrl(
        process.getCurrentOutputLines(),
        CLI_SHORT_TIMEOUT_IN_SEC);
    httpValidator.validate(url, 200, "License",
        CLI_SHORT_TIMEOUT_IN_SEC);
  }

  private static void declineLicense(ShellProcess p) {
    p.passInput("n");
  }

  private static void acceptLicense(ShellProcess p) {
    p.passInput("Y");
  }

  private void assertIfAcceptanceLicenseErrorPresent(ShellProcess p) {
    processOutputValidator.validate(
        p.getCurrentErrorLines(),
        "License acceptance is required for using StreamX",
        CLI_SHORT_TIMEOUT_IN_SEC);
  }

  private void assertIfStreamxCommandWork(ShellProcess p) {
    processOutputValidator.validate(p.getCurrentOutputLines(), "streamx-cli version",
        CLI_SHORT_TIMEOUT_IN_SEC);
  }

  private ShellProcess runStreamxCommand() {
    return terminalCommandRunner.run("--version");
  }

  private void assertIfAcceptLicenseQuestionPresent(ShellProcess p) {
    processOutputValidator.validate(
        p.getCurrentOutputLines(),
        "Do you accept the license agreement?",
        CLI_SHORT_TIMEOUT_IN_SEC);
  }

  private static void resetLicenseAcceptance() {
    try {
      String homeDir = System.getProperty("user.home");
      Path filePath = Paths.get(homeDir, ".streamx", "license.yml");
      Files.deleteIfExists(filePath);
    } catch (IOException e) {
      throw new RuntimeException("An error occurred while deleting license file", e);
    }
  }
}
