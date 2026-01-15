package dev.streamx.cli.v2.commands.config;

import io.vavr.control.Either;
import io.vavr.control.Try;
import org.jboss.logging.Logger;

import java.net.URL;
import java.nio.file.Path;

public class ConfigFile {
  private static final Logger logger = Logger.getLogger(ConfigFile.class);

  public static Either<RuntimeException, URL> getUrl() {
    String rootDir = System.getProperty("user.home");
    String dotStreamxConfig = rootDir + "/.streamx/config";

    Path pathToDir = Path.of(dotStreamxConfig);
    Path pathToFile = pathToDir.resolve("application.properties");

    return Try.of(() -> pathToFile.toUri().toURL())
      .toEither()
      .mapLeft(e -> new RuntimeException("Unable to get StreamX config path", e));
  }
}
