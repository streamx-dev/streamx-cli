package dev.streamx.cli.command.ingestion.batch;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class EventSourceDescriptor {

  public static final String FILENAME = ".eventsource.yaml";
  private String channel;
  private String key;
  private JsonNode payload;
  private Map<String, String> properties;
  private List<String> ignorePatterns;
  private Integer relativePathLevel;

  @JsonIgnore
  private Path source;

  public String getChannel() {
    return channel;
  }

  public void setChannel(String channel) {
    this.channel = channel;
  }

  public List<String> getIgnorePatterns() {
    return ignorePatterns;
  }

  public void setIgnorePatterns(List<String> ignorePatterns) {
    this.ignorePatterns = ignorePatterns;
  }

  public Integer getRelativePathLevel() {
    return relativePathLevel;
  }

  public void setRelativePathLevel(Integer relativePathLevel) {
    this.relativePathLevel = relativePathLevel;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public JsonNode getPayload() {
    return payload;
  }

  public void setPayload(JsonNode payload) {
    this.payload = payload;
  }

  public Map<String, String> getProperties() {
    return properties;
  }

  public void setProperties(Map<String, String> properties) {
    this.properties = properties;
  }

  public Path getSource() {
    return source;
  }

  public void setSource(Path source) {
    this.source = source;
  }
}
