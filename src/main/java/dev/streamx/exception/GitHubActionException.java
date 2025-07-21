package dev.streamx.exception;

public class GitHubActionException extends Exception {

  public GitHubActionException(String message) {
    super(message);
  }

  public GitHubActionException(String message, Throwable cause) {
    super(message, cause);
  }
}
