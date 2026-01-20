package dev.streamx.cli.v2.cli;

import java.util.List;

// Extend this class for commands that don't produce any output, e.g.: settings set.
public abstract class AbstractSilentCommand extends AbstractCommand<Void> {
  @Override
  public List<String> getHiddenOptions() {
    return List.of(CommonOption.OUTPUT_LONG);
  }
}
