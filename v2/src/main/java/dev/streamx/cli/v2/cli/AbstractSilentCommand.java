package dev.streamx.cli.v2.cli;

import java.util.List;

// Extend this class for commands that doesn't print anything to stdout.
public abstract class AbstractSilentCommand extends AbstractCommand {
  @Override
  public List<String> getHiddenOptions() {
    return List.of(CommonOption.OUTPUT_LONG);
  }
}
