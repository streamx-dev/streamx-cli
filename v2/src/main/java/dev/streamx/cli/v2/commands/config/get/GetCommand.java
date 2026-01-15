package dev.streamx.cli.v2.commands.config.get;

import dev.streamx.cli.v2.commands.config.ConfigFile;
import io.vavr.control.Either;
import io.vavr.control.Try;
import org.jboss.logging.Logger;
import picocli.CommandLine;

import java.net.URL;
import java.util.Properties;

@CommandLine.Command(
  name = "get",
  mixinStandardHelpOptions = true,
  description = "Get configuration property"
)
public class GetCommand implements Runnable {
  private static final Logger logger = Logger.getLogger(GetCommand.class);

  @CommandLine.Parameters(index = "0", description = "Property key")
  private String key;

  @Override
  public void run() {
    printPropertyIfExists(key).mapLeft(e -> {
      logger.error(e.getMessage());
      System.exit(1);
      return null;
    });
  }

  private Either<RuntimeException, Void> printPropertyIfExists(String key) {
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
          var value = properties.getProperty(key);
          if (value == null) {
              return Either.left(new RuntimeException("No such config property found: " + key));
          }

          logger.info(value);

          return Either.right(null);
        })
      );
  }
}