package dev.streamx.cli.v2;

import static org.assertj.core.api.Assertions.assertThat;

import dev.streamx.cli.v2.cli.AbstractCommand;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

@QuarkusTest
class MainTest {

  @Inject
  CommandLine.IFactory factory;

  @Test
  void allCommandsAndSubcommandsShouldExtendAbstractCommand() {
    CommandLine commandLine = new CommandLine(Main.class, factory);

    Set<Class<?>> allCommandClasses = new HashSet<>();
    collectAllCommands(commandLine.getCommandSpec(), allCommandClasses);

    assertThat(allCommandClasses)
      .as("All commands and subcommands should extend AbstractCommand")
      .allSatisfy(commandClass ->
        assertThat(AbstractCommand.class.isAssignableFrom(commandClass))
          .as("Command %s should extend AbstractCommand", commandClass.getName())
          .isTrue()
      );
  }

  private void collectAllCommands(CommandLine.Model.CommandSpec commandSpec, Set<Class<?>> commands) {
    Class<?> userObject = commandSpec.userObject().getClass();
    commands.add(userObject);

    for (CommandLine subcommand : commandSpec.subcommands().values()) {
      collectAllCommands(subcommand.getCommandSpec(), commands);
    }
  }
}