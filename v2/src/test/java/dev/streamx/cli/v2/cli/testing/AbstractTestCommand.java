package dev.streamx.cli.v2.cli.testing;

import dev.streamx.cli.v2.cli.AbstractCommand;
import dev.streamx.cli.v2.cli.CommandResult;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

// Helper class for testing AbstractCommand
public class AbstractTestCommand<ResultT> extends AbstractCommand<ResultT> {
  public Supplier<CommandResult<ResultT>> runCommandHandler;
  public Supplier<List<String>> hiddenOptionsHandler;
  public Function<CommandResult<ResultT>, String> getTextOutputHandler;

  public void setRunCommandHandler(Supplier<CommandResult<ResultT>> handler) {
    this.runCommandHandler = handler;
  }

  public void setHiddenOptionsHandler(Supplier<List<String>> handler) {
    this.hiddenOptionsHandler = handler;
  }

  public void setGetTextOutputHandler(Function<CommandResult<ResultT>, String> handler) {
    this.getTextOutputHandler = handler;
  }

  @Override
  public CommandResult<ResultT> runCommand() throws RuntimeException {
    if (runCommandHandler != null) {
      return runCommandHandler.get();
    }
    throw new IllegalStateException("No run command handler set");
  }

  @Override
  public List<String> getHiddenOptions() {
    if (hiddenOptionsHandler != null) {
      return hiddenOptionsHandler.get();
    }
    return super.getHiddenOptions();
  }

  @Override
  public String getTextOutput(CommandResult<ResultT> result) throws RuntimeException {
    if (getTextOutputHandler != null) {
      return getTextOutputHandler.apply(result);
    }
    return super.getTextOutput(result);
  }

  @Override
  protected Terminal createTerminal() throws IOException {
    return TerminalBuilder.builder()
      .system(false)
      .streams(System.in, System.out)
      .dumb(true)
      .build();
  }
}
