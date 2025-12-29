package dev.streamx.cli.command.ingestion;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.fasterxml.jackson.databind.JsonNode;
import com.streamx.clients.ingestion.StreamxClient;
import com.streamx.clients.ingestion.exceptions.StreamxClientException;
import com.streamx.clients.ingestion.publisher.Publisher;
import dev.streamx.cli.exception.IngestionClientException;
import dev.streamx.cli.model.Resource;
import dev.streamx.cli.util.ExceptionUtils;
import io.cloudevents.CloudEvent;
import io.cloudevents.jackson.JsonCloudEventData;
import jakarta.inject.Inject;
import java.nio.ByteBuffer;
import javax.net.ssl.SSLHandshakeException;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

public abstract class BaseIngestionCommand implements Runnable {

  @ArgGroup(exclusive = false)
  IngestionArguments ingestionArguments;

  @Spec
  protected CommandSpec spec;

  @Inject
  StreamxClientProvider streamxClientProvider;

  @Inject
  IngestionClientConfig ingestionClientConfig;

  protected abstract void perform(Publisher publisher) throws StreamxClientException;

  @Override
  public final void run() {
    try (StreamxClient client = streamxClientProvider.createStreamxClient(ingestionClientConfig)) {
      Publisher publisher = client.newPublisher();
      perform(publisher);
    } catch (StreamxClientException e) {
      if (e.getCause() instanceof SSLHandshakeException) {
        throw IngestionClientException.sslException(ingestionClientConfig.url());
      }
      throw ExceptionUtils.sneakyThrow(e);
    }
  }

  protected static CloudEvent withAdjustedData(CloudEvent event) {
    JsonCloudEventData data = (JsonCloudEventData) event.getData();
    Resource resource = extractResource(data);
    if (resource != null) {
      // convert data to format expected by StreamX
      return CloudEventBuilder.copyWithNewData(event, resource);
    }
    return event;
  }

  private static Resource extractResource(JsonCloudEventData data) {
    if (data != null) {
      JsonNode dataNode = data.getNode();
      if (dataNode != null) {
        JsonNode contentNode = dataNode.get("content");
        JsonNode typeNode = dataNode.get("type");
        String type = typeNode != null ? typeNode.asText() : null;
        if (contentNode != null) {
          JsonNode bytesNode = contentNode.get("bytes");
          if (bytesNode != null) {
            String content = bytesNode.asText();
            if (content != null) {
              // TODO: detect if the content.bytes field is already base 64 encoded
              //  and if it is encoded - decode it first.
              //  For now assume it's in clear text
              return new Resource(ByteBuffer.wrap(content.getBytes(UTF_8)), type);
            }
          }
        }
      }
    }
    return null;
  }
}
