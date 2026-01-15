package dev.streamx.cli.v2.commands.settings.get;

import dev.streamx.cli.v2.commands.settings.SettingsCommand;
import dev.streamx.cli.v2.commands.settings.SettingsFile;
import dev.streamx.cli.v2.errors.ErrorPrinter;
import io.vavr.control.Either;
import io.vavr.control.Try;
import org.jboss.logging.Logger;
import picocli.CommandLine;

import java.util.Properties;

@CommandLine.Command(
  name = "get",
  mixinStandardHelpOptions = true,
  description = "Get configuration property"
)
public class GetCommand implements Runnable {
  private static final Logger logger = Logger.getLogger(GetCommand.class);

  @CommandLine.ParentCommand
  public SettingsCommand settingsCommand;

  @CommandLine.Parameters(index = "0", description = "Property key")
  private String key;

  @Override
  public void run() {
    printPropertyIfExists(key).mapLeft(e -> {
      ErrorPrinter.print(logger, e, settingsCommand.mainCommand.verbose);
      System.exit(1);
      return null;
    });
  }

  private Either<RuntimeException, Void> printPropertyIfExists(String key) {
    return SettingsFile.getUrl()
      .flatMap(url -> Try.withResources(url::openStream)
        .of(input -> {
          Properties properties = new Properties();
          properties.load(input);
          return properties;
        })
        .toEither()
        .mapLeft(e -> new RuntimeException("Unable to load settings file", e))
        .flatMap(properties -> {
          var value = properties.getProperty(key);
          if (value == null) {
            return Either.left(new RuntimeException("No such settings property found: " + key));
          }

          logger.info(value);

          return Either.right(null);
        })
      );
  }
}