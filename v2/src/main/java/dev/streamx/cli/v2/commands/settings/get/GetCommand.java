package dev.streamx.cli.v2.commands.settings.get;

import dev.streamx.cli.v2.cli.AbstractCommand;
import dev.streamx.cli.v2.cli.CommandResult;
import dev.streamx.cli.v2.commands.settings.SettingsFile;
import picocli.CommandLine;

import java.io.IOException;
import java.util.Optional;
import java.util.Properties;

@CommandLine.Command(
  name = "get",
  mixinStandardHelpOptions = true,
  description = "Get configuration property"
)
public class GetCommand extends AbstractCommand {
  @CommandLine.Parameters(index = "0", description = "Property key")
  private String key;

  @Override
  public CommandResult runCommand() throws RuntimeException {
    var url = SettingsFile.getUrl();

    try (var inputStream = url.openStream()) {
      Properties properties = new Properties();
      properties.load(inputStream);

      var value = properties.getProperty(key);
      if (value == null) {
        throw new RuntimeException("No such settings property found: " + key);
      }

      var json = String.format("{\"key\": \"%s\", \"value\": \"%s\"}", key, value);

      return new CommandResult(Optional.of(value), Optional.of(json));
    } catch (IOException e) {
      throw new RuntimeException("Unable to get settings property", e);
    }
  }
}