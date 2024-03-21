package dev.streamx.cli.exception;

public class UnableToConnectIngestionServiceException extends RuntimeException {

  private final String ingestionUrl;

  public UnableToConnectIngestionServiceException(String ingestionUrl) {
    this.ingestionUrl = ingestionUrl;
  }

  @Override
  public String getMessage() {
    return """
        Unable to connect ingestion service.
        
        IngestionUrl: %s
        
        Verify:
         * if mesh is up and running,
         * provided ingestionUrl is set properly (if it's not - add proper '--ingestionUrl' argument)"""
        .formatted(ingestionUrl);
  }
}
