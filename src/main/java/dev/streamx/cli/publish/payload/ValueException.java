package dev.streamx.cli.publish.payload;

public class ValueException extends RuntimeException {
  private ValueException(String message) {
    super(message);
  }

  public static ValueException noJsonPathFoundException(String valueArg) {
    return new ValueException("Could not extract jsonPath from arg: " + valueArg);
  }
}
