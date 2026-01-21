package dev.streamx.cli.v2.cli.testing;

import dev.streamx.cli.v2.cli.AbstractCommand;
import dev.streamx.cli.v2.cli.CommandResult;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

// Helper class for testing AbstractCommand and it's subclasses
class AbstractTestCommand<ResultT> extends AbstractCommand<ResultT> {
  private Supplier<CommandResult<ResultT>> runCommandHandler;
  private Supplier<List<String>> hiddenOptionsHandler;
  private Function<CommandResult<ResultT>, Optional<String>> getTextOutputHandler;

  public void setRunCommandHandler(Supplier<CommandResult<ResultT>> handler) {
    this.runCommandHandler = handler;
  }

  public void setHiddenOptionsHandler(Supplier<List<String>> handler) {
    this.hiddenOptionsHandler = handler;
  }

  public void setGetTextOutputHandler(Function<CommandResult<ResultT>, Optional<String>> handler) {
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
  public Optional<String> getTextOutput(CommandResult<ResultT> result) throws RuntimeException {
    if (getTextOutputHandler != null) {
      return getTextOutputHandler.apply(result);
    }
    return super.getTextOutput(result);
  }
}
