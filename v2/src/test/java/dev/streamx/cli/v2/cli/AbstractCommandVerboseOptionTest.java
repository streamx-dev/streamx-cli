package dev.streamx.cli.v2.cli;

import dev.streamx.cli.v2.cli.testing.AbstractCommandBaseTest;
import dev.streamx.cli.v2.cli.testing.AbstractTestCommand;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AbstractCommandVerboseOptionTest extends AbstractCommandBaseTest {
  @Test
  void ifProvided_printsStackTrace() {
    var command = new AbstractTestCommand<>();
    command.setRunCommandHandler(() -> {
      throw new RuntimeException("Test exception");
    });
    var commandLine = new CommandLine(command);
    commandLine.parseArgs(CommonOption.VERBOSE_LONG);

    command.execute();

    assertTrue(errStream.toString().contains("java.lang.RuntimeException: Test exception"));
  }

  @Test
  void ifNotProvided_doesntPrintStackTrace() {
    var command = new AbstractTestCommand<>();
    command.setRunCommandHandler(() -> {
      throw new RuntimeException("Test exception");
    });
    new CommandLine(command);
    command.execute();

    assertFalse(errStream.toString().contains("java.lang.RuntimeException: Test exception"));
  }
}