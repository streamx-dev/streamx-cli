package dev.streamx.cli.exception;

import com.fasterxml.jackson.core.JsonParseException;
import com.jayway.jsonpath.JsonPath;

public class JsonPathReplacementException extends RuntimeException {

  private JsonPathReplacementException(String message, Exception exception) {
    super(message, exception);
  }

  public static JsonPathReplacementException noJsonPathFoundException(String valueArg) {
    return new JsonPathReplacementException("""
        Could not find valid jsonPath in given option.

        Option: %s

        Verify:
         * if given jsonPath is valid (according to https://github.com/json-path/JsonPath docs),
         * if '=' is present in option"""
        .formatted(valueArg), null);
  }

  public static JsonPathReplacementException jsonParseException(JsonParseException exception,
      JsonPath jsonPath, String value) {
    return new JsonPathReplacementException("""
        Replacement is not recognised as valid JSON.

        Supplied JsonPath:
        %s
        Supplied replacement:
        %s

        Make sure that:
         * you need JSON node as replacement
            (alternatively use '-s' to specify raw text replacement
            or use '-b' to specify is binary replacement),
         * it's valid JSON,
         * object property names are properly single-quoted (') or double-quoted ("),
         * strings are properly single-quoted (') or double-quoted (")

        Details: %s"""
        .formatted(jsonPath.getPath(), value, exception.getMessage()), exception);
  }

  public static JsonPathReplacementException genericJsonProcessingException(Exception exception,
      JsonPath jsonPath, String value) {
    return new JsonPathReplacementException("""
        Replacement could not be parsed.

        Supplied JsonPath:
        %s
        Supplied replacement:
        %s

        Details: %s"""
        .formatted(jsonPath.getPath(), value, exception.getMessage()), exception);
  }

  public static JsonPathReplacementException pathNotFoundException(JsonPath jsonPath) {
    return new JsonPathReplacementException("""
        JsonPath could not be found.

        Supplied JsonPath:
        %s"""
        .formatted(jsonPath.getPath()), null);
  }

}
