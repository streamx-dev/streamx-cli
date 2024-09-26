package dev.streamx.cli.config;

import static java.nio.file.StandardOpenOption.CREATE;

import io.smallrye.config.PropertiesConfigSource;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

public class DotStreamxConfigSource extends PropertiesConfigSource {

  public static final String CONFIG_SOURCE_NAME = "DotStreamxConfigSource";
  /**
   * Value of higher priority than Classpath Properties,
   * but lower than $PWD/config/application.properties
   */
  public static final int DOT_STREAMX_PRIORITY = 255;

  public DotStreamxConfigSource() throws IOException {
    super(getUrl(), DOT_STREAMX_PRIORITY);
  }

  public static URL getUrl() throws IOException {
    String rootDir = System.getProperty("user.home");
    String dotStreamxConfigSourcePath = rootDir + "/.streamx/config";

    Path pathToDir = Path.of(dotStreamxConfigSourcePath);
    Path pathToFile = pathToDir.resolve("application.properties");
    File file = createIfNotExists(pathToDir, pathToFile);

    return file.toURI().toURL();
  }

  @NotNull
  private static File createIfNotExists(Path pathToDir, Path pathToFile) throws IOException {
    File file = pathToFile.toFile();
    if (!file.exists()) {
      Files.createDirectories(pathToDir);
      Files.writeString(pathToFile, StringUtils.EMPTY, CREATE);
    }

    return file;
  }

  @Override
  public String getName() {
    return CONFIG_SOURCE_NAME;
  }
}
