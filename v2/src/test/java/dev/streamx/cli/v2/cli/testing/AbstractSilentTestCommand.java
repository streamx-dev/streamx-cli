package dev.streamx.cli.v2.cli.testing;

import dev.streamx.cli.v2.cli.AbstractCommand;
import dev.streamx.cli.v2.cli.AbstractSilentCommand;
import dev.streamx.cli.v2.cli.CommandResult;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

// Helper class for testing SilentAbstractCommand
public class AbstractSilentTestCommand extends AbstractSilentCommand {
  public Supplier<CommandResult<Void>> runCommandHandler;
  public Supplier<List<String>> hiddenOptionsHandler;

  public void setRunCommandHandler(Supplier<CommandResult<Void>> handler) {
    this.runCommandHandler = handler;
  }
  public void setHiddenOptionsHandler(Supplier<List<String>> handler) {
    this.hiddenOptionsHandler = handler;
  }

  @Override
  public CommandResult<Void> runCommand() throws RuntimeException {
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
}
