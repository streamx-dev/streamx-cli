package dev.streamx.cli.command.ingestion.publish.payload.typed;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import dev.streamx.cli.command.ingestion.PayloadProcessing;
import dev.streamx.cli.command.ingestion.publish.payload.source.RawPayload;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.avro.util.internal.JacksonUtils;

@ApplicationScoped
public class TypedPayloadFragmentResolver {

  @Inject
  @PayloadProcessing
  ObjectMapper objectMapper;

  public TypedPayload resolveFragment(RawPayload rawPayload, SourceType sourceType)
      throws IOException {
    final byte[] bytes = rawPayload.source();
    return switch (sourceType) {
      case JSON -> new TypedPayload(objectMapper.readTree(bytes));
      case BINARY -> new TypedPayload(JacksonUtils.toJsonNode(bytes));
      case STRING -> {
        String content = new String(bytes, StandardCharsets.UTF_8);

        yield new TypedPayload(TextNode.valueOf(content));
      }
    };
  }
}
