package dev.streamx.cli.ingestion.publish.payload;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;

class PropertyCreatingJacksonJsonNodeJsonProvider extends JacksonJsonNodeJsonProvider {
  PropertyCreatingJacksonJsonNodeJsonProvider(ObjectMapper objectMapper) {
    super(objectMapper);
  }

  @Override
  public Object getMapValue(Object obj, String key) {
    Object o = super.getMapValue(obj, key);
    if (o == UNDEFINED) {
      ObjectNode newChild = objectMapper.createObjectNode();
      if (obj instanceof ObjectNode objectNode) {
        objectNode.set(key, newChild);
      }
      return newChild;
    } else {
      return o;
    }
  }
}
