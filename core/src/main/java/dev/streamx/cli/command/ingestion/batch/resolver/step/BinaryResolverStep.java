package dev.streamx.cli.command.ingestion.batch.resolver.step;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.TokenBuffer;
import dev.streamx.cli.exception.PayloadException;
import dev.streamx.cli.util.FileSourceUtils;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

@ApplicationScoped
public class BinaryResolverStep implements ResolverStep {

  private static final ObjectMapper BINARY_SERIALIZATION_OBJECT_MAPPER = new ObjectMapper();

  @Override
  public JsonNode resolve(JsonNode payload, Map<String, String> variables) {
    return evaluateBinary(payload);
  }

  /**
   * Recursively replaces any text node values that start with "file://" with a JSON node
   * representing the file's binary content.
   */
  private JsonNode evaluateBinary(JsonNode evaluatedPayload) {
    if (evaluatedPayload.isTextual()) {
      String text = evaluatedPayload.textValue();
      if (FileSourceUtils.applies(text)) {
        return toJsonNode(FileSourceUtils.resolve(text));
      } else {
        return evaluatedPayload;
      }
    } else if (evaluatedPayload.isObject()) {
      ObjectNode objectNode = evaluatedPayload.deepCopy();
      Iterator<Entry<String, JsonNode>> fields = objectNode.fields();
      while (fields.hasNext()) {
        Map.Entry<String, JsonNode> entry = fields.next();
        objectNode.set(entry.getKey(), evaluateBinary(entry.getValue()));
      }
      return objectNode;
    } else if (evaluatedPayload.isArray()) {
      ArrayNode arrayNode = evaluatedPayload.deepCopy();
      for (int i = 0; i < arrayNode.size(); i++) {
        arrayNode.set(i, evaluateBinary(arrayNode.get(i)));
      }
      return arrayNode;
    } else {
      return evaluatedPayload;
    }
  }

  /**
   * Converts binary data into a JSON node by writing it as an ISO_8859_1 string.
   */
  private static JsonNode toJsonNode(byte[] datum) {
    if (datum == null) {
      return null;
    }
    try (var generator = new TokenBuffer(BINARY_SERIALIZATION_OBJECT_MAPPER, false)) {
      generator.writeString(new String(datum, StandardCharsets.ISO_8859_1));
      return BINARY_SERIALIZATION_OBJECT_MAPPER.readTree(generator.asParser());
    } catch (IOException e) {
      throw PayloadException.ioException(e);
    }
  }
}
