package dev.streamx.cli.ingestion.publish.payload;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.InvalidJsonException;
import com.jayway.jsonpath.JsonPath;
import dev.streamx.cli.exception.PayloadException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.BiConsumer;
import org.apache.commons.lang3.StringUtils;

public class PayloadResolverUtils {
  public static final String AT_FILE_SIGN = "@";
  public static final String NULL_JSON_SOURCE = "null";


  static String readStringContent(String argument) {
    if (argument.startsWith(AT_FILE_SIGN)) {
      return new String(readFile(argument.substring(1)), StandardCharsets.UTF_8);
    } else {
      return argument;
    }
  }

  static byte[] readContent(String data) {
    if (data.startsWith(AT_FILE_SIGN)) {
      return readFile(data.substring(1));
    } else {
      return data.getBytes(StandardCharsets.UTF_8);
    }
  }

  static byte[] readFile(String data) {
    Path path = Path.of(data);
    try {
      return Files.readAllBytes(path);
    } catch (NoSuchFileException e) {
      throw PayloadException.noSuchFileException(e, path);
    } catch (IOException e) {
      throw PayloadException.fileReadingException(e, path);
    }
  }

  static DocumentContext prepareWrappedJsonNode(String data,
      BiConsumer<JsonParseException, String> onJsonParseException,
      BiConsumer<JsonProcessingException, String> onJsonProcessingException
  ) {
    String source = null;
    try {
      source = readStringContent(data);

      String nullSafeSource = Optional.of(source)
          .filter(StringUtils::isNotEmpty)
          .orElse(NULL_JSON_SOURCE);

      return JsonPath.parse(nullSafeSource);
    } catch (InvalidJsonException e) {
      Throwable cause = e.getCause();
      if (cause instanceof JsonParseException exception) {
        onJsonParseException.accept(exception, source);
      } else if (cause instanceof JsonProcessingException exception) {
        onJsonProcessingException.accept(exception, source);
      }
      throw PayloadException.ioException(e);
    }
  }
}
