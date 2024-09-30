package dev.streamx.cli.config;

import static dev.streamx.cli.util.FileUtils.createIfNotExists;

import io.smallrye.config.PropertiesConfigSource;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;

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

  @Override
  public String getName() {
    return CONFIG_SOURCE_NAME;
  }
}
