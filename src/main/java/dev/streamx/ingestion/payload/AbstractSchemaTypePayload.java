package dev.streamx.ingestion.payload;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.TokenBuffer;
import dev.streamx.exception.GitHubActionException;
import dev.streamx.ingestion.IngestionPayload;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;

abstract class AbstractSchemaTypePayload implements IngestionPayload {

  private static final ObjectMapper BINARY_SERIALIZATION_OBJECT_MAPPER = new ObjectMapper();

  private final String schemaType;

  private String type;

  AbstractSchemaTypePayload(String schemaType) {
    this.schemaType = schemaType;
  }

  public String getSchemaType() {
    return schemaType;
  }

  public void setType(String type) {
    this.type = type;
  }

  protected Map<String, String> getIngestionProperties() {
    Map<String, String> properties = new HashMap<>();
    if (StringUtils.isNotBlank(type)) {
      properties.put(TYPE_KEY, type);
    }
    return properties;
  }

  protected JsonNode toJsonNode(byte[] datum) throws GitHubActionException {
    if (Objects.isNull(datum)) {
      return null;
    }

    try (var generator = new TokenBuffer(BINARY_SERIALIZATION_OBJECT_MAPPER, false)) {
      generator.writeString(new String(datum, StandardCharsets.ISO_8859_1));

      return BINARY_SERIALIZATION_OBJECT_MAPPER.readTree(generator.asParser());
    } catch (IOException exc) {
      throw new GitHubActionException(String
          .format("Failed to serialize binary data: %s", exc.getMessage()), exc);
    }
  }
}
