package dev.streamx.cli.v2.cli;

import dev.streamx.cli.v2.cli.testing.AbstractCommandBaseTest;
import dev.streamx.cli.v2.cli.testing.AbstractTestCommand;
import dev.streamx.cli.v2.cli.testing.TestObject;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.ByteArrayInputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/*
<argLine>--enable-native-access=ALL-UNNAMED -Dorg.jline.terminal.dumb=true</argLine>
 */
class AbstractCommandTest extends AbstractCommandBaseTest {
  @Test
  void execute_success() {
    var command = new AbstractTestCommand<TestObject>();
    command.setRunCommandHandler(() -> new CommandResult<>(TestObject.random()));
    new CommandLine(command);
    var exitCode = command.execute();

    assertEquals(0, exitCode);
    assertFalse(outStream.toString().isEmpty());
    assertTrue(errStream.toString().isEmpty());
  }

  @Test
  void execute_fail() {
    var command = new AbstractTestCommand<TestObject>();
    command.setRunCommandHandler(() -> {
      throw new RuntimeException("Test exception");
    });
    new CommandLine(command);
    var exitCode = command.execute();

    assertEquals(1, exitCode);
    assertTrue(outStream.toString().isEmpty());
    assertTrue(errStream.toString().contains("Test exception"));
  }

  @Test
  void getHiddenOptionsOverride() {
    var command1 = new AbstractTestCommand<>();
    new CommandLine(command1);

    assertNotNull(command1.spec.findOption(CommonOption.OUTPUT_LONG));

    var command2 = new AbstractTestCommand<>();
    command2.setHiddenOptionsHandler(() -> List.of(CommonOption.OUTPUT_LONG));
    new CommandLine(command2);

    assertNull(command2.spec.findOption(CommonOption.OUTPUT_LONG));
  }

  @Test
  void testPromptForInputMultipleCalls() {
    var command = new AbstractTestCommand<>();
    CommandLine commandLine = new CommandLine(command);
    command.setSpec(commandLine.getCommandSpec());

    String input1 = "first\n";
    System.setIn(new ByteArrayInputStream(input1.getBytes()));
    String result1 = command.promptForInput("Enter first:", null);

    assertEquals("first", result1);

    String input2 = "second\n";
    System.setIn(new ByteArrayInputStream(input2.getBytes()));
    String result2 = command.promptForInput("Enter second:", null);

    assertEquals("second", result2);
  }
}