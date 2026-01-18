package dev.streamx.cli.v2;

import dev.streamx.cli.v2.cli.ShortErrorMessageHandler;
import dev.streamx.cli.v2.commands.settings.SettingsCommand;
import io.quarkus.picocli.runtime.annotations.TopCommand;
import picocli.CommandLine;

@TopCommand
@CommandLine.Command(
  name = "streamx",
  mixinStandardHelpOptions = true,
  description = "StreamX CLI. More info at https://streamx.dev",
  subcommands = {
    SettingsCommand.class
  }
)
public class Main implements Runnable {
  @CommandLine.Spec
  CommandLine.Model.CommandSpec commandSpec;

  @Override
  public void run() {
    commandSpec
      .commandLine()
      .setParameterExceptionHandler(new ShortErrorMessageHandler())
      .usage(System.out);
  }
}
