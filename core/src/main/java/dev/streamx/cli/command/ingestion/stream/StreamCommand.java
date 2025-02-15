package dev.streamx.cli.command.ingestion.stream;

import static dev.streamx.cli.util.Output.printf;

import com.fasterxml.jackson.databind.JsonNode;
import dev.streamx.cli.VersionProvider;
import dev.streamx.cli.command.ingestion.BaseIngestionCommand;
import dev.streamx.cli.command.ingestion.stream.parser.StreamIngestionJsonParser;
import dev.streamx.cli.util.ExceptionUtils;
import dev.streamx.clients.ingestion.exceptions.StreamxClientException;
import dev.streamx.clients.ingestion.publisher.Publisher;
import dev.streamx.clients.ingestion.publisher.SuccessResult;
import jakarta.inject.Inject;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;

@Command(name = StreamCommand.COMMAND_NAME,
    mixinStandardHelpOptions = true,
    versionProvider = VersionProvider.class,
    description = "Send stream of messages from file"
)
public class StreamCommand extends BaseIngestionCommand {

  public static final String COMMAND_NAME = "stream";

  @ArgGroup(exclusive = false, multiplicity = "1")
  StreamIngestionArguments streamIngestionArguments;
  @Inject
  StreamIngestionJsonParser ingestionJsonParser;

  @Override
  protected String getChannel() {
    return streamIngestionArguments.getChannel();
  }

  @Override
  protected void perform(Publisher<JsonNode> publisher) throws StreamxClientException {
    Path streamFile = Paths.get(streamIngestionArguments.getSourceFile());

    try (FileInputStream fis = new FileInputStream(streamFile.toFile())) {

      ingestionJsonParser.parse(fis, message -> {

        String action = getRequiredProperty(message, "action");
        String key = getRequiredProperty(message, "key");

        SuccessResult result = publisher.send(message);

        printf("Sent data %s message using stream to '%s' with key '%s' at %d%n",
            action, getChannel(), key, result.getEventTime());
      });

    } catch (IOException | IllegalArgumentException e) {
      throw new RuntimeException(
          ExceptionUtils.appendLogSuggestion(
              "Error performing stream publication using '" + streamFile + "' file.\n"
                  + "\n"
                  + "Details:\n"
                  + e.getMessage()), e);
    }
  }

  private static String getRequiredProperty(JsonNode message, String property) {
    if (!message.has(property) || !message.get(property).isTextual()) {
      throw new IllegalArgumentException("Missing or invalid '%s' field".formatted(property));
    }
    return message.get(property).asText();
  }
}