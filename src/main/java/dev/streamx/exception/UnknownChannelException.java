package dev.streamx.exception;

public class UnknownChannelException extends GitHubActionException {

  public UnknownChannelException(String message) {
    super(message);
  }
}
