package dev.streamx.cli.command.ingestion.batch.exception;

import java.io.IOException;
import java.nio.file.Path;

public class EventSourceDescriptorException extends IOException {

  private final Path path;

  public EventSourceDescriptorException(Path path, Throwable cause) {
    super(cause);
    this.path = path;
  }

  public Path getPath() {
    return path;
  }
}
