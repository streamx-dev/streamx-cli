package dev.streamx.cli;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import picocli.CommandLine;
import picocli.CommandLine.IExecutionExceptionHandler;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.ParseResult;

@ApplicationScoped
public class ExceptionHandler implements IExecutionExceptionHandler {

  @Inject
  Logger log;

  @Override
  public int handleExecutionException(Exception ex, CommandLine cmd,
      ParseResult parseResult) {
    printErrorMessage(ex, cmd);
    log.error("Execution exception occurred.", ex);

    return cmd.getExitCodeExceptionMapper() == null
        ? cmd.getCommandSpec().exitCodeOnExecutionException()
        : cmd.getExitCodeExceptionMapper().getExitCode(ex);
  }

  private static void printErrorMessage(Exception ex, CommandLine cmd) {
    Throwable exceptionCause = ex;
    if (ex instanceof CommandLine.ExecutionException e && e.getCause() != null) {
      exceptionCause = e.getCause();
    }
    cmd.getErr().println(cmd.getColorScheme().errorText(exceptionCause.getMessage()));

    if (ex instanceof ParameterException && "Missing required subcommand".equals(ex.getMessage())) {
      cmd.usage(cmd.getErr());
    }
  }
}
