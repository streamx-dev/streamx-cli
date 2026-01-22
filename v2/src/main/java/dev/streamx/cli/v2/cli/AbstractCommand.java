package dev.streamx.cli.v2.cli;

import static dev.streamx.cli.v2.i18n.MessageProvider.msg;

import org.jetbrains.annotations.Nullable;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import picocli.CommandLine;
import picocli.CommandLine.Model.CommandSpec;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Optional;

/**
 * Each CLI command should extend this class.
 *
 * @param <ResultT> Must be serializable by Jackson (POJO, JsonSerializable, etc.)
 */
public abstract class AbstractCommand<ResultT> implements Runnable {
  // Override this method to implement the command logic.
  public abstract CommandResult<ResultT> runCommand() throws RuntimeException;

  // Override this method to hide specific command line options.
  // May be useful to hide the "--output" option for commands that don't print anything in case of success.
  public List<String> getHiddenOptions() {
    return List.of();
  }

  // Override this method to provide human-readable output.
  public String getTextOutput(CommandResult<ResultT> result) throws RuntimeException {
    return result.toText(OutputFormat.json, null);
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
  public CommandSpec spec;

  @CommandLine.Spec
  public void setSpec(CommandSpec spec) {
    this.spec = spec;
    applyHiddenOptions();
  }

  @CommandLine.Option(
    names = {CommonOption.VERBOSE_SHORT, CommonOption.VERBOSE_LONG},
    description = "Print debug information"
  )
  public boolean verbose;

  @CommandLine.Option(
    names = {CommonOption.OUTPUT_SHORT, CommonOption.OUTPUT_LONG},
    description = "Specify output format: text, json, yaml",
    defaultValue = "text"
  )
  // Explicitly set default value here as a fallback for commands with the hidden output option.
  public OutputFormat output = OutputFormat.text;

  public void printUsage() {
    spec.commandLine().usage(System.out);
  }

  // For testing purposes mostly.
  protected Terminal createTerminal() throws IOException {
    return TerminalBuilder.builder().system(true).build();
  }

  // Use this method for asking user input in interactive commands.
  public String promptForInput(String prompt, @Nullable List<String> autocompleteOptions) throws RuntimeException {
    try (Terminal terminal = createTerminal()) {
      LineReaderBuilder builder = LineReaderBuilder.builder()
        .terminal(terminal);

      Completer completer = null;
      if (autocompleteOptions != null) {
        completer = new StringsCompleter(autocompleteOptions);
        builder.completer(completer);
      }

      LineReader reader = builder.build();

      return reader.readLine(completer == null ? prompt : prompt + " (TAB for autocomplete):").strip();
    } catch (IOException e) {
      throw new RuntimeException(msg.failedToHandleInteractiveInput(), e);
    }
  }

  public int execute() {
    int exitCode = 0;

    try {
      var textOutput = this.runCommand().toText(output, this::getTextOutput);
      if (!textOutput.isEmpty()) {
        System.out.println(textOutput);
      }
    } catch (Exception e) {
      exitCode = ShortErrorMessageHandler.shortErrorMessage(e, spec.commandLine());
      if (verbose) {
        // Print exception stacktrace
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        System.err.println(sw);
      }
    }

    return exitCode;
  }

  public void run() {
    int exitCode = execute();
    System.exit(exitCode);
  }
}
