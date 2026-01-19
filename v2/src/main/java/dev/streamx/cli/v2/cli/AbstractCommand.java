package dev.streamx.cli.v2.cli;

import picocli.CommandLine;
import picocli.CommandLine.Model.CommandSpec;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

// Each CLI command should extend this class.
public abstract class AbstractCommand implements Runnable {
  // Override this method to implement the command logic.
  public abstract CommandResult runCommand() throws RuntimeException;

  // Override this method to hide specific command line options.
  // May be useful to hide the "--output" option for commands that don't print anything in case of success.
  public List<String> getHiddenOptions() {
    return List.of();
  }

  private void applyHiddenOptions() {
    var options = getHiddenOptions();

    for (String option : options) {
      var optionSpec = spec.findOption(option);
      if (optionSpec != null) {
        spec.remove(optionSpec);
      }
    }
  }

  @CommandLine.Spec
  private CommandSpec spec;

  @CommandLine.Spec
  private void setSpec(CommandSpec spec) {
    this.spec = spec;
    applyHiddenOptions();
  }

  @CommandLine.Option(
    names = {CommonOption.VERBOSE_SHORT, CommonOption.VERBOSE_LONG},
    description = "Print debug information"
  )
  private boolean verbose;

  @CommandLine.Option(
    names = {CommonOption.OUTPUT_SHORT, CommonOption.OUTPUT_LONG},
    description = "Specify output format: text, json, yaml",
    defaultValue = "text"
  )
  private OutputFormat outputFormat;

  private void validateSubcommands() {
    for (CommandLine subcommand : spec.subcommands().values()) {
      Object userObject = subcommand.getCommandSpec().userObject();
      if (!(userObject instanceof AbstractCommand)) {
        throw new RuntimeException(
          "All subcommands must extend AbstractCommand: " +
            subcommand.getCommandName()
        );
      }
    }
  }

  public void printUsage() {
    spec.commandLine().usage(System.out);
  }

  public void run() {
    validateSubcommands();

    try {
      var result = this.runCommand();
      result.print(outputFormat);
    } catch (Exception e) {
      int exitCode = ShortErrorMessageHandler.shortErrorMessage(e, spec.commandLine());
      if (verbose) {
        // Print exception stacktrace
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        System.err.println(sw);
      }

      System.exit(exitCode);
    }
  }
}
