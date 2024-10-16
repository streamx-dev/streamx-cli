package dev.streamx.cli.exception;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import dev.streamx.cli.util.ExceptionUtils;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

public class PayloadException extends RuntimeException {

  private PayloadException(String message, Exception exception) {
    super(message, exception);
  }

  private PayloadException(String message) {
    super(message);
  }

  public static PayloadException payloadNotFound() {
    return new PayloadException("""
        Payload definition not found.

        Make sure that:
         * there is an optional third parameter that points to the file containing the payload,
         * or there is at least one @|bold,italic Payload Defining Option|@ (-s/-b/-j)""");
  }

  public static PayloadException jsonParseException(JsonParseException exception, String payload) {
    return new PayloadException("""
        Payload could not be parsed.

        Supplied payload:
        %s

        Make sure that:
         * it's valid JSON,
         * object property names are properly single-quoted (') or double-quoted ("),
         * strings are properly single-quoted (') or double-quoted (")

        Details: %s""".formatted(payload, exception.getMessage()), exception);
  }

  public static PayloadException genericJsonProcessingException(JsonProcessingException exception,
      String payload) {
    return new PayloadException("""
        Payload could not be parsed.

        Supplied payload:
        %s

        Details: %s""".formatted(payload, exception.getMessage()), exception);
  }

  public static PayloadException noSuchFileException(NoSuchFileException exception, Path path) {
    return new PayloadException("File does not exist.\nPath: " + path, exception);
  }

  public static PayloadException fileReadingException(IOException exception, Path path) {
    return new PayloadException("Could not read file.\nPath: " + path + "\n"
                                + "Details: " + exception.getMessage(), exception);
  }

  public static PayloadException ioException(Exception exception) {
    return new PayloadException(ExceptionUtils.appendLogSuggestion(
        exception.getMessage()), exception);
  }
}
