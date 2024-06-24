package dev.streamx.cli.config;

import static java.nio.file.StandardOpenOption.CREATE;

import io.quarkus.runtime.configuration.ApplicationPropertiesConfigSourceLoader.InClassPath;
import io.quarkus.runtime.configuration.ApplicationPropertiesConfigSourceLoader.InFileSystem;
import io.smallrye.config.PropertiesConfigSource;
import io.smallrye.config.SysPropConfigSource;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;

public class DotStreamxConfigSource extends PropertiesConfigSource {

  public static final String CONFIG_SOURCE_NAME = "DotStreamxConfigSource";
  /**
   * Value higher priority than {@link InClassPath Classpath Properties}
   * but lower than {@link InFileSystem $PWD/config/application.properties}
   */
  public static final int DOT_STREAMX_PRIORITY = 255;

  public DotStreamxConfigSource() throws IOException {
    super(getUrl(), DOT_STREAMX_PRIORITY);
  }

  private static URL getUrl() throws IOException {
    String rootDir = System.getProperty("user.home");
    String dotStreamxConfigSourcePath = rootDir + "/.streamx/config";

    Path path = Path.of(dotStreamxConfigSourcePath);
    Path pathToFile = path.resolve("application.properties");
    File file = createIfNotExists(pathToFile, path);

    return file.toURI().toURL();
  }

  @NotNull
  private static File createIfNotExists(Path pathToFile, Path path) throws IOException {
    File file = pathToFile.toFile();
    if (!file.exists()) {
      Files.createDirectories(path);
      Files.writeString(pathToFile, "", CREATE);
    }

    return file;
  }

  @Override
  public String getName() {
    return CONFIG_SOURCE_NAME;
  }
}
