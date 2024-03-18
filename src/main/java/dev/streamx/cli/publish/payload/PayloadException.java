package dev.streamx.cli.publish.payload;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;

public class PayloadException extends RuntimeException {
  private PayloadException(String prefix, Exception exception) {
    super(prefix + exception.getMessage(), exception);
  }

  public static PayloadException jsonProcessingException(JsonProcessingException exception) {
    return new PayloadException("Json processing exception. ", exception);
  }

  public static PayloadException fileReadingException(IOException exception) {
    return new PayloadException("File reading exception. Path: ", exception);
  }

  public static PayloadException ioException(IOException exception) {
    return new PayloadException("Input-output exception. ", exception);
  }
}
