package dev.streamx.cli.exception;

public class UnableToConnectIngestionServiceException extends RuntimeException {

  private static final String MESSAGE = """
                                            Unable to connect ingestion service.
                                                    
                                            IngestionUrl: %s
                                                    
                                            Verify:
                                            """
                                        + "* if mesh is up and running,\n"
                                        + "* provided ingestionUrl is set properly"
                                        + " (if it's not - add proper '--ingestionUrl' option)";

  private final String ingestionUrl;

  public UnableToConnectIngestionServiceException(String ingestionUrl) {
    this.ingestionUrl = ingestionUrl;
  }

  @Override
  public String getMessage() {
    return MESSAGE.formatted(ingestionUrl);
  }
}
