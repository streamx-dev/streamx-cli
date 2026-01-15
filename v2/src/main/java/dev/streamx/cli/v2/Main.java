package dev.streamx.cli.v2;

import dev.streamx.cli.v2.commands.config.ConfigCommand;
import io.quarkus.picocli.runtime.annotations.TopCommand;
import picocli.CommandLine;

@TopCommand
@CommandLine.Command(
  name = "streamx",
  mixinStandardHelpOptions = true,
  description = "StreamX CLI. More info at https://streamx.dev",
  subcommands = {
    ConfigCommand.class
  }
)
public class Main implements Runnable {
  @CommandLine.Spec
  CommandLine.Model.CommandSpec commandSpec;

  @CommandLine.Option(
    names = {"-v", "--verbose"},
    description = "Print debug information"
  )
  private Boolean verbose = false;

  @Override
  public void run() {
    if (verbose) {
      System.setProperty("quarkus.log.level", "DEBUG");
    }

    commandSpec.commandLine().usage(System.out);
  }
}
