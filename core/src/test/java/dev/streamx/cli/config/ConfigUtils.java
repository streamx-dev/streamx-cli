package dev.streamx.cli.config;

import static java.nio.file.StandardOpenOption.CREATE;

import dev.streamx.cli.util.ExceptionUtils;
import io.quarkus.runtime.configuration.QuarkusConfigFactory;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ConfigUtils {

  private ConfigUtils() {

  }

  public static void clearConfigCache() {
    QuarkusConfigFactory.setConfig(null);
  }

  public static void installFile(String path, String content) {
    try {
      if (path.contains("/")) {
        Files.createDirectories(Path.of(path.substring(0, path.lastIndexOf("/"))));
      }

      Files.writeString(Path.of(path), content, CREATE);
    } catch (Exception e) {
      ExceptionUtils.sneakyThrow(e);
    }
  }

  public static void clearConfigFile(String path) {
    File out = new File(path);
    if (out.isFile()) {
      out.delete();
    }
  }
}
