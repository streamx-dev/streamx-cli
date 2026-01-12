package dev.streamx.cli.command.ingestion.stream.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import java.util.Base64;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

public class JsonBase64Encoder {

  private JsonBase64Encoder() {
    // no instances
  }

  public static void encodeFields(JsonNode root, List<String> fieldPaths) {
    for (String fieldPath : fieldPaths) {
      fieldPath = StringUtils.prependIfMissing(fieldPath, "/");
      JsonNode node = root.at(fieldPath);

      if (node.isMissingNode() || !node.isTextual()) {
        continue;
      }

      TextNode encodedTextNode = createTextNodeWithEncodedContent(node);
      replaceNode(root, fieldPath, encodedTextNode);
    }
  }

  private static TextNode createTextNodeWithEncodedContent(JsonNode node) {
    String encodedText = Base64.getEncoder().encodeToString(node.asText().getBytes());
    return TextNode.valueOf(encodedText);
  }

  private static void replaceNode(JsonNode root, String fieldPath, JsonNode newValue) {
    String parentPath = StringUtils.substringBeforeLast(fieldPath, "/");
    String fieldName = StringUtils.substringAfterLast(fieldPath, "/");

    JsonNode parentNode = root.at(parentPath);
    if (parentNode instanceof ObjectNode obj) {
      obj.set(fieldName, newValue);
    }
  }
}