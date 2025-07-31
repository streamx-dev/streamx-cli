package dev.streamx.exception;

public class MissingRequiredInputException extends GitHubActionException {

  public MissingRequiredInputException(String message) {
    super(message);
  }
}
