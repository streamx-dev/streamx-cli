package dev.streamx.cli.v2.cli;

import java.util.List;

// Extend this class for commands that don't do anything except display their subcommands.
public class AbstractCommandGroup extends AbstractCommand {
  @Override
  public CommandResult runCommand() {
    this.printUsage();
    return CommandResult.empty();
  }

  @Override
  public List<String> getHiddenOptions() {
    return List.of(
      CommonOption.OUTPUT_LONG,
      CommonOption.VERBOSE_LONG
    );
  }
}
