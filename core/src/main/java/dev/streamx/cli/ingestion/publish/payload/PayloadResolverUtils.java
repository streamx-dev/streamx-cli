package dev.streamx.cli.ingestion.publish.payload;

import dev.streamx.cli.exception.PayloadException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

class PayloadResolverUtils {
  private static final String AT_FILE_SIGN = "@";

  static String readStringContent(String argument) {
    if (argument.startsWith(AT_FILE_SIGN)) {
      return new String(readFile(argument), StandardCharsets.UTF_8);
    } else {
      return argument;
    }
  }

  static byte[] readContent(String data) {
    if (data.startsWith(AT_FILE_SIGN)) {
      return readFile(data);
    } else {
      return data.getBytes(StandardCharsets.UTF_8);
    }
  }

  static byte[] readFile(String data) {
    Path path = Path.of(data.substring(1));
    try {
      return Files.readAllBytes(path);
    } catch (NoSuchFileException e) {
      throw PayloadException.noSuchFileException(e, path);
    } catch (IOException e) {
      throw PayloadException.fileReadingException(e, path);
    }
  }
}
