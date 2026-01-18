package dev.streamx.cli.v2.commands.settings.set;

import dev.streamx.cli.v2.cli.AbstractCommand;
import dev.streamx.cli.v2.cli.CommandResult;
import dev.streamx.cli.v2.cli.CommonOption;
import dev.streamx.cli.v2.commands.settings.SettingsFile;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

@CommandLine.Command(
  name = "set",
  mixinStandardHelpOptions = true,
  description = "Set configuration property"
)
public class SetCommand extends AbstractCommand {
  @CommandLine.Parameters(index = "0", description = "Property key")
  private String key;

  @CommandLine.Parameters(index = "1", description = "Property value")
  private String value;

  @Override
  public List<String> getHiddenOptions() {
    return List.of(CommonOption.OUTPUT_LONG);
  }

  @Override
  public CommandResult runCommand() throws RuntimeException {
    var url = SettingsFile.getUrl();

    try (
      var inputStream = url.openStream();
      var outputStream = Files.newOutputStream(Paths.get(url.getPath()));
    ) {
      Properties properties = new Properties();
      properties.load(inputStream);
      properties.setProperty(key, value);
      properties.store(outputStream, null);
    } catch (IOException e) {
      throw new RuntimeException("Unable to set settings property", e);
    }

    return CommandResult.empty();
  }
}