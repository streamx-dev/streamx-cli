package dev.streamx.cli.v2.cli;

import dev.streamx.cli.v2.cli.testing.AbstractCommandBaseTest;
import dev.streamx.cli.v2.cli.testing.AbstractSilentTestCommand;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import static org.junit.jupiter.api.Assertions.*;

class AbstractSilentCommandTest extends AbstractCommandBaseTest {
  @Test
  void outputFlagIsAbsent() {
    var command = new AbstractSilentTestCommand();
    new CommandLine(command); // Trigger all PicocLi initialization

    assertNull(command.spec.findOption(CommonOption.OUTPUT_LONG));
  }

  @Test
  void outputIsEmpty() {
    var command = new AbstractSilentTestCommand();
    command.setRunCommandHandler(() -> new CommandResult<>(null));
    command.execute();

    assertEquals("", outStream.toString());
    assertEquals("", errStream.toString());
  }
}