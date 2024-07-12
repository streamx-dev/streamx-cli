package dev.streamx.cli.test.tools.terminal;

import dev.streamx.cli.test.tools.terminal.command.OsCommandStrategy;
import dev.streamx.cli.test.tools.terminal.process.ShellProcess;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.jboss.logging.Logger;

@ApplicationScoped
@Named("OsCommandRunner")
public class OsTerminalCommandRunner implements TerminalCommandRunner {

  private final Logger logger = Logger.getLogger(OsTerminalCommandRunner.class);
  private final List<ShellProcess> processes;
  @Inject
  OsCommandStrategy osCommand;

  public OsTerminalCommandRunner() {
    processes = new LinkedList<>();
  }

  public ShellProcess run(String command) {
    logger.info("Running terminal command: " + command);
    ProcessBuilder processBuilder = osCommand.create(command);
    try {
      ShellProcess shellProcess = ShellProcess.run(processBuilder);
      logger.info("Terminal command: '" + command + "' started");
      processes.add(shellProcess);
      return shellProcess;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @PreDestroy
  public void cleanUp() {
    for (ShellProcess process : processes) {
      process.destroy();
      logger.info("Process destroyed PID: " + process.pid());
    }
    processes.clear();
  }
}
