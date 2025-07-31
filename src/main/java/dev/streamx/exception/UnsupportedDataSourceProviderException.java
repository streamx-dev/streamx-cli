package dev.streamx.exception;

public class UnsupportedDataSourceProviderException extends GitHubActionException {

  public UnsupportedDataSourceProviderException(String message) {
    super(message);
  }
}
