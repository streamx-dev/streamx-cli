package dev.streamx.cli.v2.cli;

import picocli.CommandLine;

import java.io.PrintWriter;

import picocli.CommandLine.IParameterExceptionHandler;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.UnmatchedArgumentException;

public class ShortErrorMessageHandler implements IParameterExceptionHandler {

  @Override
  public int handleParseException(ParameterException ex, String[] args) {
    CommandLine cmd = ex.getCommandLine();
    return shortErrorMessage(ex, cmd);
  }

  static int shortErrorMessage(Exception ex, CommandLine cmd) {
    PrintWriter writer = cmd.getErr();
    String errorMessage = ex.getMessage();

    writer.println(cmd.getColorScheme().errorText(errorMessage));
    if (ex instanceof ParameterException) {
      UnmatchedArgumentException.printSuggestions((ParameterException) ex, writer);
    }

    if (ex instanceof ParameterException || ex instanceof IllegalArgumentException) {
      CommandSpec spec = cmd.getCommandSpec();
      writer.printf("Try '%s%s' for more information on the available options.%n", spec.qualifiedName(), "help".equals(spec.name()) ? "" : " --help");
      return cmd.getCommandSpec().exitCodeOnInvalidInput();
    }
    return cmd.getCommandSpec().exitCodeOnExecutionException();
  }

}