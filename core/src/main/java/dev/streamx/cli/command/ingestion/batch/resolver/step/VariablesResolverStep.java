package dev.streamx.cli.command.ingestion.batch.resolver.step;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import dev.streamx.cli.command.ingestion.batch.resolver.substitutor.Substitutor;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

@ApplicationScoped
public class VariablesResolverStep implements ResolverStep {

  @Inject
  Substitutor substitutor;

  @Override
  public JsonNode resolve(JsonNode payload, Map<String, String> variables) {
    return evaluateVariables(payload, variables);
  }

  /**
   * Recursively replaces variables in any text node values. Variables are of the form
   * ${variableName} and are replaced using the provided map.
   */
  private JsonNode evaluateVariables(JsonNode rawPayload, Map<String, String> variables) {
    if (rawPayload.isTextual()) {
      String text = rawPayload.textValue();
      return TextNode.valueOf(substitutor.substitute(variables, text));
    } else if (rawPayload.isObject()) {
      ObjectNode objectNode = rawPayload.deepCopy();
      Iterator<Entry<String, JsonNode>> fields = objectNode.fields();
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
}
