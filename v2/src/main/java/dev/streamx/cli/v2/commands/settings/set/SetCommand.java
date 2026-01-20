package dev.streamx.cli.v2.commands.settings.set;

import dev.streamx.cli.v2.cli.AbstractSilentCommand;
import dev.streamx.cli.v2.cli.CommandResult;
import dev.streamx.cli.v2.commands.settings.SettingsFile;

import static dev.streamx.cli.v2.i18n.MessageProvider.msg;

import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

@CommandLine.Command(
  name = "set",
  mixinStandardHelpOptions = true,
  description = "Set configuration property"
)
public class SetCommand extends AbstractSilentCommand {
  @CommandLine.Parameters(index = "0", description = "Property key")
  private String key;

  @CommandLine.Parameters(index = "1", description = "Property value")
  private String value;

  @Override
  public CommandResult<Void> runCommand() throws RuntimeException {
    var url = SettingsFile.getUrl();
    var path = Paths.get(url.getPath());

    Properties properties = new Properties();

    try (var inputStream = url.openStream()) {
      properties.load(inputStream);
    } catch (IOException e) {
      throw new RuntimeException(msg.unableToSetSettingsProperty(), e);
    }

    properties.setProperty(key, value);

    try (var outputStream = Files.newOutputStream(path)) {
      properties.store(outputStream, null);
    } catch (IOException e) {
      throw new RuntimeException(msg.unableToSetSettingsProperty(), e);
    }

    return new CommandResult<>(null);
  }
}