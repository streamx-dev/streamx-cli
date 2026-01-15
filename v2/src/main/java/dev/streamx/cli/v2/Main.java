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
    names = {"--log-level"},
    description = "Set logging level (TRACE, DEBUG, INFO, WARN, ERROR)",
    defaultValue = "INFO"
  )
  private String logLevel;

  private void setLogLevel(String level) {
    System.setProperty("quarkus.log.level", level);
  }

  @Override
  public void run() {
    setLogLevel(logLevel);
    commandSpec.commandLine().usage(System.out);
  }
}
