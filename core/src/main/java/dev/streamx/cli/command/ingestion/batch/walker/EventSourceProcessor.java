package dev.streamx.cli.command.ingestion.batch.walker;

import dev.streamx.cli.command.ingestion.batch.EventSourceDescriptor;
import java.io.IOException;
import java.nio.file.Path;

@FunctionalInterface
public interface EventSourceProcessor {

  /**
   * Processes a file with the given active EventSourceModel.
   *
   * @param file   the file  to process
   * @param currentDescriptor the active EventSourceModel for this directory (or null if none)
   */
  void apply(Path file, EventSourceDescriptor currentDescriptor) throws IOException;
}
