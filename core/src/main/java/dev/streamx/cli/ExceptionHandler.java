package dev.streamx.cli;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import picocli.CommandLine;
import picocli.CommandLine.IExecutionExceptionHandler;
import picocli.CommandLine.ParseResult;

@ApplicationScoped
public class ExceptionHandler implements IExecutionExceptionHandler {

  @Inject
  Logger log;

  @Override
  public int handleExecutionException(Exception ex, CommandLine cmd,
      ParseResult parseResult) {
    Throwable throwable = ex;
    if (ex instanceof CommandLine.ExecutionException e) {
      throwable = e.getCause();
    }
    cmd.getErr().println(cmd.getColorScheme().errorText(throwable.getMessage()));
    log.error("Execution exception occurred.", throwable);

    return cmd.getExitCodeExceptionMapper() == null
        ? cmd.getCommandSpec().exitCodeOnExecutionException()
        : cmd.getExitCodeExceptionMapper().getExitCode(throwable);
  }
}
