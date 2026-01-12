package dev.streamx.cli.command.ingestion.stream;

import static dev.streamx.cli.util.Output.printf;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.streamx.ce.serialization.DeserializerException;
import com.streamx.ce.serialization.json.CloudEventJsonDeserializer;
import com.streamx.clients.ingestion.exceptions.StreamxClientException;
import com.streamx.clients.ingestion.publisher.Publisher;
import dev.streamx.cli.VersionProvider;
import dev.streamx.cli.command.ingestion.BaseIngestionCommand;
import dev.streamx.cli.command.ingestion.stream.parser.JsonBase64Encoder;
import dev.streamx.cli.command.ingestion.stream.parser.StreamIngestionJsonParser;
import dev.streamx.cli.util.ExceptionUtils;
import dev.streamx.cli.util.FileUtils;
import io.cloudevents.CloudEvent;
import jakarta.inject.Inject;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;

@Command(name = StreamCommand.COMMAND_NAME,
    mixinStandardHelpOptions = true,
    versionProvider = VersionProvider.class,
    description = "Send stream of events from file"
)
public class StreamCommand extends BaseIngestionCommand {

  // TODO "_v2" is a temporary postfix for now
  public static final String COMMAND_NAME = "stream_v2";

  private static final ObjectMapper objectMapper = new ObjectMapper();
  private static final CloudEventJsonDeserializer deserializer = new CloudEventJsonDeserializer();

  @ArgGroup(exclusive = false, multiplicity = "1")
  StreamIngestionArguments streamIngestionArguments;

  @Inject
  StreamIngestionJsonParser ingestionJsonParser;

  @Override
  protected void perform(Publisher publisher) throws StreamxClientException {
    Path streamFile = Paths.get(streamIngestionArguments.getSourceFile());
    List<String> jsonFieldsToEncodeToBase64 = StreamProperties
        .getJsonFieldsToEncodeToBase64(streamFile);

    try (FileInputStream fis = new FileInputStream(streamFile.toFile())) {

      ingestionJsonParser.parse(fis, cloudEventNode -> {
        JsonBase64Encoder.encodeFields(cloudEventNode, jsonFieldsToEncodeToBase64);
        CloudEvent inputEvent = toCloudEvent(cloudEventNode);
        CloudEvent responseEvent = publisher.send(inputEvent);
        printf("Sent %s event using stream with key '%s' at %s%n",
            inputEvent.getType(), inputEvent.getSubject(), responseEvent.getTime());
      });

    } catch (IOException | DeserializerException e) {
      throw new RuntimeException(
          ExceptionUtils.appendLogSuggestion(
              "Error performing stream publication using '"
              + FileUtils.toString(streamFile) + "' file.\n"
              + "\n"
              + "Details:\n"
              + e.getMessage()), e);
    }
  }

  private static CloudEvent toCloudEvent(JsonNode cloudEventNode) throws JsonProcessingException {
    byte[] cloudEventJsonBytes = objectMapper.writeValueAsBytes(cloudEventNode);
    return deserializer.deserialize(cloudEventJsonBytes);
  }
}
