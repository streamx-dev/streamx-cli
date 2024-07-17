package dev.streamx.cli.exception;

public class UnableToConnectIngestionServiceException extends RuntimeException {

  private static final String MESSAGE = """
        Unable to connect to the ingestion service.
                
        The ingestion service URL: %s
                
        Verify:
         * if the mesh is up and running,
         * if the ingestion service URL is set correctly \
         (if it's not - set proper '--ingestionUrl' option)""";

  private final String ingestionUrl;

  public UnableToConnectIngestionServiceException(String ingestionUrl) {
    this.ingestionUrl = ingestionUrl;
  }

  @Override
  public String getMessage() {
    return MESSAGE.formatted(ingestionUrl);
  }
}
