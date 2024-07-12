package dev.streamx.cli.test.tools.terminal;

import dev.streamx.cli.StreamxTerminalCommand;
import dev.streamx.cli.test.tools.terminal.process.ShellProcess;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@ApplicationScoped
@Named("StreamxCommandRunner")
public class StreamxTerminalCommandRunnerDecorator implements TerminalCommandRunner {

  @Inject
  @Named("OsCommandRunner")
  TerminalCommandRunner wrappee;
  @Inject
  StreamxTerminalCommand streamxTerminalCommand;


  @Override
  public ShellProcess run(String command) {
    return wrappee.run(streamxTerminalCommand.getCommand() + " " + command);
  }
}
