package dev.streamx.cli.v2;

import dev.streamx.cli.v2.cli.AbstractCommandGroup;
import dev.streamx.cli.v2.cli.CommandResult;
import dev.streamx.cli.v2.cli.ShortErrorMessageHandler;
import dev.streamx.cli.v2.commands.interactive.InteractiveCommand;
import dev.streamx.cli.v2.commands.settings.SettingsCommand;
import io.quarkus.picocli.runtime.annotations.TopCommand;
import picocli.CommandLine;

@TopCommand
@CommandLine.Command(
  name = "streamx",
  mixinStandardHelpOptions = true,
  description = "StreamX CLI. More info at https://streamx.dev",
  subcommands = {
    SettingsCommand.class,
    InteractiveCommand.class
  }
)
public class Main extends AbstractCommandGroup {
  @CommandLine.Spec
  CommandLine.Model.CommandSpec commandSpec;

  @Override
  public CommandResult<Void> runCommand() throws RuntimeException {
    commandSpec
      .commandLine()
      .setParameterExceptionHandler(new ShortErrorMessageHandler())
      .usage(System.out);

    return new CommandResult<>(null);
  }
}
