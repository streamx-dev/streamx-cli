package dev.streamx.cli.v2.commands.settings;

import dev.streamx.cli.v2.Main;
import dev.streamx.cli.v2.commands.settings.get.GetCommand;
import dev.streamx.cli.v2.commands.settings.set.SetCommand;
import picocli.CommandLine;

@CommandLine.Command(
  name = "settings",
  mixinStandardHelpOptions = true,
  description = "Modify StreamX settings",
  abbreviateSynopsis = true,
  synopsisHeading = "Synopsis example",
  subcommands = {
//    ListCommand.class,
    SetCommand.class,
    GetCommand.class
  }
)
public class SettingsCommand implements Runnable {
  @CommandLine.Spec
  CommandLine.Model.CommandSpec commandSpec;

  @CommandLine.ParentCommand
  public Main mainCommand;

  public void run() {
    commandSpec.commandLine().usage(System.out);
  }
}
