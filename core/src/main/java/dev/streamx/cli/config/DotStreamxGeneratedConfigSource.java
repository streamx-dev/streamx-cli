package dev.streamx.cli.config;

import static dev.streamx.cli.util.FileUtils.createIfNotExists;

import io.smallrye.config.PropertiesConfigSource;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;

public class DotStreamxGeneratedConfigSource extends PropertiesConfigSource {

  public static final String CONFIG_SOURCE_NAME = "DotStreamxGeneratedConfigSource";
  /**
   * Value lower than classpath application.properties
   */
  public static final int DOT_STREAMX_GENERATED_PRIORITY = 240;

  public DotStreamxGeneratedConfigSource() throws IOException {
    super(getUrl(), DOT_STREAMX_GENERATED_PRIORITY);
  }

  public static URL getUrl() throws IOException {
    Path pathToDir = getConfigDir();
    Path pathToFile = pathToDir.resolve("application.properties");
    File file = createIfNotExists(pathToDir, pathToFile);

    return file.toURI().toURL();
  }

  public static Path getConfigDir() {
    String rootDir = System.getProperty("user.home");
    String dotStreamxConfigSourcePath = rootDir + "/.streamx/config/generated";

    return Path.of(dotStreamxConfigSourcePath);
  }

  @Override
  public String getName() {
    return CONFIG_SOURCE_NAME;
  }
}
