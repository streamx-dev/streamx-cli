package dev.streamx.cli.test.tools.terminal;

import dev.streamx.cli.StreamxCommand;
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
  StreamxCommand streamxCommand;


  @Override
  public Process run(String command) {
    return wrappee.run(streamxCommand.getCommand() + " " + command);
  }
}
