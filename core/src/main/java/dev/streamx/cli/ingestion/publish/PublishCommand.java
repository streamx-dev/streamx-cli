package dev.streamx.cli.ingestion.publish;

import com.fasterxml.jackson.databind.JsonNode;
import dev.streamx.cli.SchemaProvider;
import dev.streamx.cli.VersionProvider;
import dev.streamx.cli.exception.UnknownChannelException;
import dev.streamx.cli.ingestion.BaseIngestionCommand;
import dev.streamx.cli.ingestion.publish.payload.PayloadResolver;
import dev.streamx.cli.ingestion.publish.payload.source.FileSourceResolver;
import dev.streamx.clients.ingestion.exceptions.StreamxClientException;
import dev.streamx.clients.ingestion.publisher.Publisher;
import dev.streamx.clients.ingestion.publisher.SuccessResult;
import jakarta.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Spec;

@Command(name = PublishCommand.COMMAND_NAME,
    mixinStandardHelpOptions = true,
    versionProvider = VersionProvider.class,
    description = "Send publication data")
public class PublishCommand extends BaseIngestionCommand {

  public static final String COMMAND_NAME = "publish";

  @ArgGroup(exclusive = false, multiplicity = "1")
  PublishTargetArguments publishTargetArguments;

  @ArgGroup(exclusive = false)
  PayloadArguments payloadArguments;

  @Inject
  PayloadResolver payloadResolver;

  @Inject
  SchemaProvider schemaProvider;

  @Inject
  IngestionMessageJsonFactory ingestionMessageJsonFactory;

  @Override
  protected String getChannel() {
    return publishTargetArguments.getChannel();
  }

  @Override
  protected void perform(Publisher<JsonNode> publisher) throws StreamxClientException {
    List<PayloadArgument> mergedPayloadArgumentList = prependPayloadFile();
    JsonNode payload = payloadResolver.createPayload(mergedPayloadArgumentList);
    JsonNode schemaJson = null;
    try {
      schemaJson = schemaProvider.getSchema(getChannel());
    } catch (UnknownChannelException e) {
      throw new ParameterException(spec.commandLine(),
          "Channel '" + e.getChannel() + "' not found. "
              + "Available channels: " + e.getAvailableChannels());
    }
    String payloadPropertyName = getPayloadPropertyName(schemaJson);
    JsonNode requestPayload = ingestionMessageJsonFactory.from(
        publishTargetArguments.getKey(),
        COMMAND_NAME,
        payload,
        payloadPropertyName
    );

    SuccessResult result = publisher.send(requestPayload);
    System.out.printf("Registered data publication on '%s' with key '%s' at %d%n",
        publishTargetArguments.getChannel(), result.getKey(), result.getEventTime());
  }

  private String getPayloadPropertyName(JsonNode schemaJson) {
    Schema.Parser parser = new Schema.Parser();
    Schema channelschema = parser.parse(schemaJson.toString());
    Field payload = channelschema.getField("payload");
    Schema payloadSchema = payload.schema();
    if (payloadSchema.getType() == Type.UNION) {
      List<Schema> unionSchemas = payloadSchema.getTypes();
      for (Schema schema : unionSchemas) {
        if (schema.getType() == Type.RECORD) {
          return schema.getFullName();
        }
      }
    }
    return payloadSchema.getFullName();
  }

  @NotNull
  private List<PayloadArgument> prependPayloadFile() {
    var payloadFileArgument = Optional.ofNullable(publishTargetArguments)
        .map(PublishTargetArguments::getPayloadFile)
        .map(arg -> PayloadArgument.ofJsonNode(FileSourceResolver.FILE_STRATEGY_PREFIX + arg));
    var payloadArgStream = Optional.ofNullable(payloadArguments)
        .map(PayloadArguments::getPayloadArgs).stream()
        .flatMap(Collection::stream);

    return Stream.concat(payloadFileArgument.stream(), payloadArgStream).toList();
  }
}
