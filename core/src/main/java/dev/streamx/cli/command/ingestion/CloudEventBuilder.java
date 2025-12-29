package dev.streamx.cli.command.ingestion;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import io.cloudevents.CloudEvent;
import io.cloudevents.jackson.JsonCloudEventData;
import java.net.URI;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

public class CloudEventBuilder {

  private static final ObjectMapper objectMapper = new ObjectMapper();

  private CloudEventBuilder() {
    // no instance
  }

  public static CloudEvent build(String subject, String type, String source, Object data) {
    JsonNode dataNode = objectMapper.valueToTree(data);
    return build(subject, type, source, dataNode);
  }

  public static CloudEvent build(String subject, String type, String source, JsonNode data) {
    var builder = io.cloudevents.core.builder.CloudEventBuilder.v1()
        .withId(UUID.randomUUID().toString())
        .withSource(URI.create(source))
        .withSubject(subject)
        .withType(type)
        .withTime(OffsetDateTime.now(ZoneOffset.UTC));

    if (data == null || data instanceof NullNode) {
      return builder.withoutData().build();
    }

    return builder
        .withDataContentType("application/json")
        .withData(JsonCloudEventData.wrap(data))
        .build();
  }

  public static CloudEvent copyWithNewData(CloudEvent cloudEvent, JsonNode data) {
    return io.cloudevents.core.builder.CloudEventBuilder.v1(cloudEvent)
        .withDataContentType("application/json")
        .withData(JsonCloudEventData.wrap(data))
        .build();
  }

}
