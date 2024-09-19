package dev.streamx.cli.ingestion.publish;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class IngestionMessageJsonFactory {

  private static final ObjectMapper mapper = new ObjectMapper();

  /**
   * @param key of resource
   * @param action publish or unpublish
   * @param payloadContent to include as a payload in returned JsonNode
   * @param nameOfObjectToWrapPayload
   * @return JsonNode representation of {@link dev.streamx.clients.ingestion.publisher.Message}
   */

  public JsonNode from(
      String key,
      String action,
      JsonNode payloadContent,
      String nameOfObjectToWrapPayload
  ) {
    ObjectNode root = mapper.createObjectNode();

    root.put("key", key);
    root.put("action", action);
    root.putNull("eventTime");
    root.putObject("properties");
    ObjectNode payload = root.putObject("payload");
    payload.set(nameOfObjectToWrapPayload, payloadContent);
    return root;
  }
}
