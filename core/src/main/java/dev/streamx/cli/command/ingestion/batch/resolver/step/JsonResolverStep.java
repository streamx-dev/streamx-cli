package dev.streamx.cli.command.ingestion.batch.resolver.step;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.streamx.cli.exception.PayloadException;
import dev.streamx.cli.util.JsonSourceUtils;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

@ApplicationScoped
public class JsonResolverStep implements ResolverStep {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Override
  public JsonNode resolve(JsonNode payload, Map<String, String> variables) {
    return evaluateJson(payload);
  }

  /**
   * Recursively replaces any text node values that start with "json://" with a JSON node
   * representing the file's JSON content.
   */
  private JsonNode evaluateJson(JsonNode evaluatedPayload) {
    if (evaluatedPayload.isTextual()) {
      String text = evaluatedPayload.textValue();
      if (JsonSourceUtils.applies(text)) {
        return toJsonNode(JsonSourceUtils.resolve(text));
      } else {
        return evaluatedPayload;
      }
    } else if (evaluatedPayload.isObject()) {
      ObjectNode objectNode = evaluatedPayload.deepCopy();
      Iterator<Entry<String, JsonNode>> fields = objectNode.fields();
      while (fields.hasNext()) {
        Entry<String, JsonNode> entry = fields.next();
        objectNode.set(entry.getKey(), evaluateJson(entry.getValue()));
      }
      return objectNode;
    } else if (evaluatedPayload.isArray()) {
      ArrayNode arrayNode = evaluatedPayload.deepCopy();
      for (int i = 0; i < arrayNode.size(); i++) {
        arrayNode.set(i, evaluateJson(arrayNode.get(i)));
      }
      return arrayNode;
    } else {
      return evaluatedPayload;
    }
  }

  private static JsonNode toJsonNode(Path jsonPath) {
    if (jsonPath == null) {
      return null;
    }
    try {
      return OBJECT_MAPPER.readTree(jsonPath.toFile());
    } catch (IOException e) {
      throw PayloadException.ioException(e);
    }
  }
}
