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
    ConfigFile.getUrl().fold(
      err -> {
        logger.error(err);
        System.exit(1);
        return null;
      },
      url -> {
        return setProperty(url, key, value).fold(
          e -> {
            System.exit(1);
            return null;
          },
          s -> null
        );
      }
    );
  }

  private Either<String, Void> setProperty(URL url, String key, String value) {
    Try<Void> result = Try.of(() -> {
      Properties properties = new Properties();

      Try.withResources(url::openStream)
        .of(input -> {
          properties.load(input);
          properties.setProperty(key, value);
          return null;
        })
        .getOrElseThrow(e -> new RuntimeException("Couldn't load config", e));

      Try.withResources(() -> Files.newOutputStream(Paths.get(url.getPath())))
        .of(output -> {
          properties.store(output, null);
          return null;
        })
        .getOrElseThrow(e -> new RuntimeException("Failed to save properties", e));

      return null;
    });

    return result.toEither().mapLeft((e) -> {
      logger.error("Failed to set property '" + key + "'");
      logger.debug(e);
      return e.getMessage();
    });
  }
}