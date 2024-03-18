package dev.streamx.cli.publish.payload;

public class PayloadException extends RuntimeException {
  public PayloadException(Exception exception) {
    super("Json processing exception. " + exception.getMessage(), exception);
  }
}
