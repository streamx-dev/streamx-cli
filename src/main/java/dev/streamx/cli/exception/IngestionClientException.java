package dev.streamx.cli.exception;

import dev.streamx.clients.ingestion.exceptions.StreamxClientException;

public class IngestionClientException extends RuntimeException {

  public IngestionClientException(StreamxClientException cause) {
    super(cause);
  }

  @Override
  public String getMessage() {
    return getCause().getMessage();
  }
}
