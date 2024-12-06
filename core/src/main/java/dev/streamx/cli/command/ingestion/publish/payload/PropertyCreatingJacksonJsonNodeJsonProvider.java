package dev.streamx.cli.command.ingestion.publish.payload;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;

/**
 * This implementation of JsonProvider creating new json objects if
 * json properties on JSONPath expressions are not available.
 * <p>
 *
 * Solution inspired by:
 * <a href="https://github.com/json-path/JsonPath/issues/83#issuecomment-929519105">this comment</a>
 */
public class PropertyCreatingJacksonJsonNodeJsonProvider extends JacksonJsonNodeJsonProvider {
  public PropertyCreatingJacksonJsonNodeJsonProvider(ObjectMapper objectMapper) {
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
