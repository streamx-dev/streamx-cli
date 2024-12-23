package dev.streamx.cli.command.cloud;

import static dev.streamx.cli.util.Output.print;

import dev.streamx.cli.VersionProvider;
import dev.streamx.cli.command.cloud.delete.DeleteCommand;
import dev.streamx.cli.command.cloud.deploy.DeployCommand;
import picocli.CommandLine.Command;

@Command(
    name = CloudCommand.COMMAND_NAME,
    mixinStandardHelpOptions = true,
    versionProvider = VersionProvider.class,
    description = "Top-level command for cloud operations.",
    subcommands = {DeployCommand.class, DeleteCommand.class}
)
public class CloudCommand implements Runnable {

  protected static final String COMMAND_NAME = "cloud";
  protected static final String MESSAGE =
      "Use a subcommand like '" + DeployCommand.COMMAND_NAME + "' or '" + DeleteCommand.COMMAND_NAME
          + "' for cloud operations.";

  @Override
  public void run() {
    print(MESSAGE);
  }
}
