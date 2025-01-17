package dev.streamx.cli.command.ingestion.publish.payload.typed;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.util.TokenBuffer;
import dev.streamx.cli.command.ingestion.PayloadProcessing;
import dev.streamx.cli.command.ingestion.publish.payload.source.RawPayload;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@ApplicationScoped
public class TypedPayloadFragmentResolver {

  private static final ObjectMapper BINARY_SERIALIZATION_OBJECT_MAPPER = new ObjectMapper();

  @Inject
  @PayloadProcessing
  ObjectMapper objectMapper;

  public TypedPayload resolveFragment(RawPayload rawPayload, SourceType sourceType)
      throws IOException {
    final byte[] bytes = rawPayload.source();
    return switch (sourceType) {
      case JSON -> new TypedPayload(objectMapper.readTree(bytes));
      case BINARY -> new TypedPayload(toJsonNode(bytes));
      case STRING -> {
        String content = new String(bytes, StandardCharsets.UTF_8);

        yield new TypedPayload(TextNode.valueOf(content));
      }
    };
  }

  private static JsonNode toJsonNode(byte[] datum) throws IOException {
    if (datum == null) {
      return null;
    }

    try (var generator = new TokenBuffer(BINARY_SERIALIZATION_OBJECT_MAPPER, false)) {
      generator.writeString(new String(datum, StandardCharsets.ISO_8859_1));

      return BINARY_SERIALIZATION_OBJECT_MAPPER.readTree(generator.asParser());
    }
  }
}
