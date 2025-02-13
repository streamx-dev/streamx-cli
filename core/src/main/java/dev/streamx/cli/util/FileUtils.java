package dev.streamx.cli.util;

import static java.nio.file.StandardOpenOption.CREATE;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

public class FileUtils {

  private FileUtils() {
    // no instance
  }

  @NotNull
  public static File createIfNotExists(Path pathToDir, Path pathToFile) throws IOException {
    File file = pathToFile.toFile();
    if (!file.exists()) {
      Files.createDirectories(pathToDir);
      Files.writeString(pathToFile, StringUtils.EMPTY, CREATE);
    }

    return file;
  }

  @NotNull
  public static Path getNthParent(Path path, int n) {
    if (path == null) {
      throw new IllegalArgumentException("Input path must not be null.");
    }
    Path current = path.normalize();
    for (int i = 0; i <= n; i++) {
      current = current.getParent();
      if (current == null) {
        throw new IllegalArgumentException(
            "Path " + path + " does not have " + n + " parent levels."
        );
      }
    }
    return current;
  }
}
