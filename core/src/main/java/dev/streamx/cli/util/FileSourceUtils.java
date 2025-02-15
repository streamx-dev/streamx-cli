package dev.streamx.cli.util;

import dev.streamx.cli.exception.PayloadException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

public class FileSourceUtils {

  public static final String FILE_STRATEGY_PREFIX = "file://";

  private FileSourceUtils() {
    // No instances
  }


  public static boolean applies(String rawSource) {
    return rawSource != null && rawSource.startsWith(FILE_STRATEGY_PREFIX);
  }

  public static byte[] resolve(String rawSource) {
    String source = rawSource.substring(FILE_STRATEGY_PREFIX.length());

    return readFile(source);
  }

  private static byte[] readFile(String data) {
    Path path = Path.of(data);
    try {
      return Files.readAllBytes(path);
    } catch (NoSuchFileException e) {
      throw PayloadException.noSuchFileException(e, path);
    } catch (IOException e) {
      throw PayloadException.fileReadingException(e, path);
    }
  }
}
