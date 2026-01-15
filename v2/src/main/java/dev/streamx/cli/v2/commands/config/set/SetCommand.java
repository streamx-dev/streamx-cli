package dev.streamx.cli.v2.commands.config.set;

import dev.streamx.cli.v2.commands.config.ConfigFile;
import io.vavr.control.Either;
import io.vavr.control.Try;
import org.jboss.logging.Logger;
import picocli.CommandLine;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

@CommandLine.Command(
  name = "set",
  mixinStandardHelpOptions = true,
  description = "Set configuration property"
)
public class SetCommand implements Runnable {
  private static final Logger logger = Logger.getLogger(SetCommand.class);

  @CommandLine.Parameters(index = "0", description = "Property key")
  private String key;

  @CommandLine.Parameters(index = "1", description = "Property value")
  private String value;

  @Override
  public void run() {
    setProperty(key, value).mapLeft(e -> {
      logger.error(e.getMessage());
      System.exit(1);
      return null;
    });
  }

  private Either<RuntimeException, Void> setProperty(String key, String value) {
    return ConfigFile.getUrl()
      .flatMap(url -> Try.withResources(url::openStream)
        .of(input -> {
          Properties properties = new Properties();
          properties.load(input);
          return properties;
        })
        .toEither()
        .mapLeft(e -> new RuntimeException("Unable to load config file", e))
        .flatMap(properties -> {
          Try.withResources(url::openStream)
            .of(input -> {
              properties.load(input);
              properties.setProperty(key, value);
              return null;
            })
            .toEither()
            .mapLeft(e -> new RuntimeException("Couldn't load config", e));

          Try.withResources(() -> Files.newOutputStream(Paths.get(url.getPath())))
            .of(output -> {
              properties.store(output, null);
              return null;
            })
            .toEither()
            .mapLeft(e -> new RuntimeException("Failed to save properties", e));

          return Either.right(null);
        })
      );
  }
}