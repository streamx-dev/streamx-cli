package dev.streamx.cli.exception;

import com.fasterxml.jackson.core.JsonParseException;
import com.jayway.jsonpath.JsonPath;

public class ValueException extends RuntimeException {

  private ValueException(String message, Exception exception) {
    super(message, exception);
  }

  public static ValueException noJsonPathFoundException(String valueArg) {
    return new ValueException("""
        Could not find valid jsonPath in given argument.
                
        Argument: %s
                
        Verify:
         * if given jsonPath is valid (according to https://github.com/json-path/JsonPath docs),
         * if '=' is present in argument"""
        .formatted(valueArg), null);
  }

  public static ValueException jsonParseException(JsonParseException exception,
      JsonPath jsonPath, String value) {
    return new ValueException("""
        Replacement is not recognised as valid JSON.

        Supplied JsonPath:
        %s
        Supplied replacement:
        %s
                
        Make sure that:
         * you need JSON as replacement
            (alternatively use '-s' to indicate replacement is raw text
            or use '-b' to indicate replacement is binary data),
         * it's valid JSON,
         * object property names are properly single-quoted (') or double-quoted ("),
         * strings are properly single-quoted (') or double-quoted (")
                
        Details: %s"""
        .formatted(jsonPath.getPath(), value, exception.getMessage()), exception);
  }

  public static ValueException genericJsonProcessingException(Exception exception,
      JsonPath jsonPath, String value) {
    return new ValueException("""
        Replacement could not be parsed.

        Supplied JsonPath:
        %s
        Supplied replacement:
        %s
                
        Details: %s"""
        .formatted(jsonPath.getPath(), value, exception.getMessage()), exception);
  }

  public static ValueException pathNotFoundException(JsonPath jsonPath) {
    return new ValueException("""
        JsonPath could not be found.

        Supplied JsonPath:
        %s"""
        .formatted(jsonPath.getPath()), null);
  }

}
