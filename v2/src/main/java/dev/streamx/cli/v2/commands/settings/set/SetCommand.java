package dev.streamx.cli.v2.commands.settings.set;

import dev.streamx.cli.v2.commands.settings.SettingsCommand;
import dev.streamx.cli.v2.commands.settings.SettingsFile;
import dev.streamx.cli.v2.errors.ErrorPrinter;
import io.vavr.control.Either;
import io.vavr.control.Try;
import org.jboss.logging.Logger;
import picocli.CommandLine;

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

  @CommandLine.ParentCommand
  public SettingsCommand settingsCommand;

  @CommandLine.Parameters(index = "0", description = "Property key")
  private String key;

  @CommandLine.Parameters(index = "1", description = "Property value")
  private String value;

  @Override
  public void run() {
    setProperty(key, value).mapLeft(e -> {
      ErrorPrinter.print(logger, e, settingsCommand.mainCommand.verbose);
      System.exit(1);
      return null;
    });
  }

  private Either<RuntimeException, Void> setProperty(String key, String value) {
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
          Try.withResources(url::openStream)
            .of(input -> {
              properties.load(input);
              properties.setProperty(key, value);
              return null;
            })
            .toEither()
            .mapLeft(e -> new RuntimeException("Couldn't load settings", e));

          Try.withResources(() -> Files.newOutputStream(Paths.get(url.getPath())))
            .of(output -> {
              properties.store(output, null);
              return null;
            })
            .toEither()
            .mapLeft(e -> new RuntimeException("Failed to save settings", e));

          return Either.right(null);
        })
      );
  }
}