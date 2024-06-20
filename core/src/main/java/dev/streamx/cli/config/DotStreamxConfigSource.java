package dev.streamx.cli.config;

import static java.nio.file.StandardOpenOption.CREATE;

import io.smallrye.config.PropertiesConfigSource;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

public class DotStreamxConfigSource extends PropertiesConfigSource {

  public static final String CONFIG_SOURCE_NAME = DotStreamxConfigSource.class.getSimpleName();
  public static final int DOT_STREAMX_PRIORITY = 255;

  public DotStreamxConfigSource() throws IOException {
    super(getUrl(), DOT_STREAMX_PRIORITY);
  }

  private static URL getUrl() throws IOException {
    String rootDir = System.getProperty("user.home");
    String dotStreamxConfigSourcePath = rootDir + "/config";

    Path path = Path.of(dotStreamxConfigSourcePath);
    Path pathToFile = path.resolve("application.properties");
    File file = pathToFile.toFile();
    if (!file.exists()) {
      Files.createDirectories(path);
      Files.writeString(pathToFile, "", CREATE);
    }

    return file.toURI().toURL();
  }

  @Override
  public String getName() {
    return CONFIG_SOURCE_NAME;
  }
}
