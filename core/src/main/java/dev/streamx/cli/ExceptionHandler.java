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

    cmd.getErr().println(cmd.getColorScheme().errorText(ex.getMessage()));
    log.error("Execution exception occurred.", ex);

    return cmd.getExitCodeExceptionMapper() == null
        ? cmd.getCommandSpec().exitCodeOnExecutionException()
        : cmd.getExitCodeExceptionMapper().getExitCode(ex);
  }
}
