package dev.streamx.cli.ingestionclient;

import dev.streamx.clients.ingestion.exceptions.StreamxClientException;

public class IngestionClientException extends RuntimeException {

  public IngestionClientException(StreamxClientException cause) {
    super("Ingestion client exception. Message: " + cause.getMessage(), cause);
  }
}
