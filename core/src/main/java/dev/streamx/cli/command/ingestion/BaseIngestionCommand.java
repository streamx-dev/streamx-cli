package dev.streamx.cli.command.ingestion;

import com.fasterxml.jackson.databind.JsonNode;
import dev.streamx.cli.SchemaProvider;
import dev.streamx.cli.exception.IngestionClientException;
import dev.streamx.cli.exception.UnableToConnectIngestionServiceException;
import dev.streamx.cli.exception.UnknownChannelException;
import dev.streamx.cli.util.ExceptionUtils;
import dev.streamx.clients.ingestion.StreamxClient;
import dev.streamx.clients.ingestion.exceptions.StreamxClientConnectionException;
import dev.streamx.clients.ingestion.exceptions.StreamxClientException;
import dev.streamx.clients.ingestion.exceptions.UnsupportedChannelException;
import dev.streamx.clients.ingestion.publisher.Publisher;
import jakarta.inject.Inject;
import java.util.List;
import javax.net.ssl.SSLHandshakeException;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Spec;

public abstract class BaseIngestionCommand implements Runnable {

  @ArgGroup(exclusive = false)
  IngestionArguments ingestionArguments;

  @Spec
  protected CommandSpec spec;

  @Inject
  StreamxClientProvider streamxClientProvider;

  @Inject
  SchemaProvider schemaProvider;

  @Inject
  IngestionClientConfig ingestionClientConfig;

  protected abstract String getChannel();

  protected abstract void perform(Publisher<JsonNode> publisher) throws StreamxClientException;

  @Override
  public final void run() {
    try (StreamxClient client = streamxClientProvider.createStreamxClient(ingestionClientConfig)) {
      doRun(client);
    } catch (UnsupportedChannelException e) {
      throw new ParameterException(spec.commandLine(), e.getMessage());
    } catch (StreamxClientConnectionException e) {
      throw new UnableToConnectIngestionServiceException(ingestionClientConfig.url(), e);
    } catch (StreamxClientException e) {
      if (e.getCause() instanceof SSLHandshakeException) {
        throw IngestionClientException.sslException(ingestionClientConfig.url());
      }
      throw ExceptionUtils.sneakyThrow(e);
    }
  }

  protected void doRun(StreamxClient client) throws StreamxClientException {
    Publisher<JsonNode> publisher = client.newPublisher(getChannel(), JsonNode.class);
    perform(publisher);
  }

  protected String getPayloadPropertyName() {
    JsonNode schemaJson = getSchemaForChannel();
    return getPayloadPropertyName(schemaJson);
  }

  private String getPayloadPropertyName(JsonNode schemaJson) {
    Schema.Parser parser = new Schema.Parser();
    Schema channelSchema = parser.parse(schemaJson.toString());
    Field payload = channelSchema.getField("payload");
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

  private JsonNode getSchemaForChannel() {
    try {
      return schemaProvider.getSchema(getChannel());
    } catch (UnknownChannelException e) {
      throw new ParameterException(spec.commandLine(),
          "Channel '" + e.getChannel() + "' not found. "
              + "Available channels: " + e.getAvailableChannels());
    }
  }
}
