package dev.streamx.cli.publish.payload;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PayloadResolver {

  private final ObjectMapper objectMapper = new ObjectMapper();

  public JsonNode createPayload(String data) {
    try {
      JsonNode jsonNode = objectMapper.readValue(data, JsonNode.class);

      // TODO support loading Payload from file
      // TODO replace jsonpath

      return jsonNode;
    } catch (JsonProcessingException e) {
      throw new PayloadException(e);
    }
  }
}
