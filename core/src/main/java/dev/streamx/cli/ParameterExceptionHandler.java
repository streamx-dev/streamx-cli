package dev.streamx.cli;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.PrintWriter;
import org.jboss.logging.Logger;
import picocli.CommandLine;
import picocli.CommandLine.IParameterExceptionHandler;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.UnmatchedArgumentException;

@ApplicationScoped
public class ParameterExceptionHandler implements IParameterExceptionHandler {

  @Inject
  Logger log;

  @Override
  public int handleParseException(ParameterException ex, String[] args) {
    log.error("Parameter exception occurred.", ex);

    CommandLine cmd = ex.getCommandLine();
    PrintWriter writer = cmd.getErr();

    writer.println(ex.getMessage());
    UnmatchedArgumentException.printSuggestions(ex, writer);
    writer.println(cmd.getHelp().fullSynopsis());

    CommandSpec spec = cmd.getCommandSpec();
    writer.printf("Try '%s --help' for more information.", spec.qualifiedName());
    writer.println();

    if (cmd.getExitCodeExceptionMapper() != null) {
      return cmd.getExitCodeExceptionMapper().getExitCode(ex);
    }
    return spec.exitCodeOnInvalidInput();
  }
}
