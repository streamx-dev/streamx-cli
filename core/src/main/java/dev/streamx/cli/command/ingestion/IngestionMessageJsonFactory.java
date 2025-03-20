package dev.streamx.cli.command.ingestion;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Map;

@ApplicationScoped
public class IngestionMessageJsonFactory {

  private static final ObjectMapper mapper = new ObjectMapper();

  /**
   * @param key            of resource
   * @param action         publish or unpublish
   * @param payloadContent to include as a payload in returned JsonNode
   * @param properties     to use as payload properties
   * @param payloadType    type matching registered ingestion API schema
   * @return JsonNode representation of {@link dev.streamx.clients.ingestion.publisher.Message}
   */

  public JsonNode from(
      String key,
      String action,
      JsonNode payloadContent,
      Map<String, String> properties,
      String payloadType
  ) {
    ObjectNode root = mapper.createObjectNode();
    ObjectNode propertiesObject = mapper.createObjectNode();

    if (properties != null) {
      properties.forEach(propertiesObject::put);
    }

    root.put("key", key);
    root.put("action", action);
    root.putNull("eventTime");
    root.set("properties", propertiesObject);
    ObjectNode payload = root.putObject("payload");
    payload.set(payloadType, payloadContent);
    return root;
  }
}
