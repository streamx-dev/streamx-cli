package dev.streamx.cli.command.ingestion.batch.resolver.step;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BinaryNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.streamx.cli.exception.PayloadException;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

@ApplicationScoped
public class BinaryResolverStep implements ResolverStep {

  private static final String FILE_STRATEGY_PREFIX = "file://";

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
      if (isFileReference(text)) {
        byte[] content = readFileContent(text);
        return new BinaryNode(content);
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

  public static boolean isFileReference(String rawSource) {
    return rawSource != null && rawSource.startsWith(FILE_STRATEGY_PREFIX);
  }

  public static byte[] readFileContent(String rawSource) {
    String sourceFile = rawSource.substring(FILE_STRATEGY_PREFIX.length());
    Path path = Path.of(sourceFile);
    try {
      return Files.readAllBytes(path);
    } catch (NoSuchFileException e) {
      throw PayloadException.noSuchFileException(e, path);
    } catch (IOException e) {
      throw PayloadException.fileReadingException(e, path);
    }
  }

}
