package dev.streamx.ingestion.payload;

import dev.streamx.ingestion.IngestionPayload;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

abstract class AbstractSchemaTypePayload implements IngestionPayload {

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
}
