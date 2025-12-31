package dev.streamx.cli.command.ingestion.stream.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import java.util.Base64;
import java.util.List;

public class JsonBase64Encoder {

  private JsonBase64Encoder() {
    // no instances
  }

  public static void encodeFields(JsonNode root, List<String> jsonPaths) {
    for (String jsonPath : jsonPaths) {
      JsonNode node = root.at(jsonPath);

      if (node.isMissingNode() || !node.isTextual()) {
        continue;
      }

      TextNode encodedTextNode = createTextNodeWithEncodedContent(node);
      replaceNode(root, jsonPath, encodedTextNode);
    }
  }

  private static TextNode createTextNodeWithEncodedContent(JsonNode node) {
    String encodedText = Base64.getEncoder().encodeToString(node.asText().getBytes());
    return TextNode.valueOf(encodedText);
  }

  private static void replaceNode(JsonNode root, String jsonPath, JsonNode newValue) {
    int lastSlash = jsonPath.lastIndexOf('/');
    String parentNodePath = jsonPath.substring(0, lastSlash);
    String fieldName = jsonPath.substring(lastSlash + 1);

    JsonNode parentNode = root.at(parentNodePath);
    if (parentNode instanceof ObjectNode obj) {
      obj.set(fieldName, newValue);
    }
  }
}