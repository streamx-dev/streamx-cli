package dev.streamx.cli.v2.commands.config;

import dev.streamx.cli.v2.Main;
import dev.streamx.cli.v2.commands.config.get.GetCommand;
import dev.streamx.cli.v2.commands.config.list.ListCommand;
import dev.streamx.cli.v2.commands.config.set.SetCommand;
import picocli.CommandLine;

@CommandLine.Command(
  name = "config",
  mixinStandardHelpOptions = true,
  description = "Modify StreamX config",
  abbreviateSynopsis = true,
  synopsisHeading = "Synopsis example",
  subcommands = {
    ListCommand.class,
    SetCommand.class,
    GetCommand.class
  }
)
public class ConfigCommand implements Runnable {
  @CommandLine.Spec
  CommandLine.Model.CommandSpec commandSpec;

  @CommandLine.ParentCommand
  public Main mainCommand;

  public void run() {
    commandSpec.commandLine().usage(System.out);
  }
}
