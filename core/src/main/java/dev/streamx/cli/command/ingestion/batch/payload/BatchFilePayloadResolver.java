package dev.streamx.cli.command.ingestion.batch.payload;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.util.TokenBuffer;
import dev.streamx.cli.command.ingestion.batch.EventSourceDescriptor;
import dev.streamx.cli.exception.PayloadException;
import dev.streamx.cli.util.FileSourceUtils;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Map;

@ApplicationScoped
public class BatchFilePayloadResolver implements BatchPayloadResolver {

  private static final ObjectMapper BINARY_SERIALIZATION_OBJECT_MAPPER = new ObjectMapper();

  @Override
  public JsonNode createPayload(EventSourceDescriptor currentDescriptor,
      Map<String, String> variables) {

    JsonNode rawPayload = currentDescriptor.getPayload();
    JsonNode evaluatedPayload = evaluateVariables(rawPayload, variables);
    return evaluateBinary(evaluatedPayload);
  }

  public Map<String, String> createSubstitutionVariables(Path file,
      EventSourceDescriptor currentDescriptor, String relativePath) {
    return Map.of(
        "payloadPath", file.toString(),
        "channel", currentDescriptor.getChannel(),
        "relativePath", relativePath
    );
  }

  public String substitute(Map<String, String> variables, String text) {
    String result = text;
    for (Map.Entry<String, String> entry : variables.entrySet()) {
      // FixMe this should be a better solution
      result = result.replace("${" + entry.getKey() + "}", entry.getValue());
    }
    return result;
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
      Iterator<Map.Entry<String, JsonNode>> fields = objectNode.fields();
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
   * Recursively replaces variables in any text node values. Variables are of the form
   * ${variableName} and are replaced using the provided map.
   */
  private JsonNode evaluateVariables(JsonNode rawPayload, Map<String, String> variables) {
    if (rawPayload.isTextual()) {
      String text = rawPayload.textValue();
      return TextNode.valueOf(substitute(variables, text));
    } else if (rawPayload.isObject()) {
      ObjectNode objectNode = rawPayload.deepCopy();
      Iterator<Map.Entry<String, JsonNode>> fields = objectNode.fields();
      while (fields.hasNext()) {
        Map.Entry<String, JsonNode> entry = fields.next();
        objectNode.set(entry.getKey(), evaluateVariables(entry.getValue(), variables));
      }
      return objectNode;
    } else if (rawPayload.isArray()) {
      ArrayNode arrayNode = rawPayload.deepCopy();
      for (int i = 0; i < arrayNode.size(); i++) {
        arrayNode.set(i, evaluateVariables(arrayNode.get(i), variables));
      }
      return arrayNode;
    } else {
      return rawPayload;
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
