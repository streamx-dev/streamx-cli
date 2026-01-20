package dev.streamx.cli.v2.commands.interactive;

import dev.streamx.cli.v2.cli.AbstractSilentCommand;
import dev.streamx.cli.v2.cli.CommandResult;
import dev.streamx.cli.v2.commands.settings.SettingsFile;
import jakarta.annotation.Nullable;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

import static dev.streamx.cli.v2.i18n.MessageProvider.msg;

@CommandLine.Command(
  name = "interactive",
  mixinStandardHelpOptions = true,
  description = "Set configuration property"
)
public class InteractiveCommand extends AbstractSilentCommand {

  @CommandLine.Parameters(
    index = "0",
    description = "Property key",
    arity = "0..1"
  )
  private String key;

  @CommandLine.Parameters(
    index = "1",
    description = "Property value",
    arity = "0..1"
  )
  private String value;

  @Override
  public CommandResult<Void> runCommand() throws RuntimeException {
    try {
      if (key == null || key.isBlank()) {
        key = promptForKey();
      }

      if (value == null || value.isBlank()) {
        value = promptForInput("Enter property value: ", null);
      }

      saveProperty(key, value);

      return new CommandResult<>(null);

    } catch (IOException e) {
      throw new RuntimeException(msg.failedToReadUserInput(), e);
    }
  }

  private String promptForKey() throws IOException {
    List<String> existingKeys = List.of("streamx.mesh.url", "streamx.some.property", "streamx.mesh.auth.token");
    Completer completer = new StringsCompleter(existingKeys);

    return promptForInput("Enter property key (TAB for autocomplete): ", completer);
  }

  private String promptForInput(String prompt, @Nullable Completer completer) throws IOException {
    try (Terminal terminal = TerminalBuilder.builder().system(true).build()) {
      LineReaderBuilder builder = LineReaderBuilder.builder()
        .terminal(terminal);

      if (completer != null) {
        builder.completer(completer);
      }

      LineReader reader = builder.build();

      return reader.readLine(prompt).strip();
    }
  }

  private void saveProperty(String key, String value) {
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
  }
}