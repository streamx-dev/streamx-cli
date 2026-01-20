package dev.streamx.cli.v2.commands.settings.get;

import dev.streamx.cli.v2.cli.AbstractCommand;
import dev.streamx.cli.v2.cli.CommandResult;
import dev.streamx.cli.v2.commands.settings.SettingsFile;

import static dev.streamx.cli.v2.i18n.MessageProvider.msg;

import picocli.CommandLine;

import java.io.IOException;
import java.util.Optional;
import java.util.Properties;

@CommandLine.Command(
  name = "get",
  mixinStandardHelpOptions = true,
  description = "Get configuration property"
)
public class GetCommand extends AbstractCommand<GetCommandResult> {
  @CommandLine.Parameters(index = "0", description = "Property key")
  private String key;

  @Override
  public Optional<String> getTextOutput(CommandResult<GetCommandResult> result) throws RuntimeException {
    return Optional.of(result.result.value());
  }

  @Override
  public CommandResult<GetCommandResult> runCommand() throws RuntimeException {
    var url = SettingsFile.getUrl();

    try (var inputStream = url.openStream()) {
      Properties properties = new Properties();
      properties.load(inputStream);

      var value = properties.getProperty(key);
      if (value == null) {
        throw new RuntimeException(msg.noSettingsPropertyFound(key));
      }

      var result = new GetCommandResult(key, value);

      return new CommandResult<>(result);
    } catch (IOException e) {
      throw new RuntimeException(msg.unableToGetSettingsProperty(), e);
    }
  }
}