package dev.streamx.ingestion.payload;

import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.UUID;

public class CloudEventFactory {

  private static final URI EVENT_SOURCE = URI.create("streamx-connector-github");
  private static final String DATA_CONTENT_TYPE = "application/json";

  public static CloudEvent createPublishEvent(String eventType, String subject, byte[] content) {
    String base64Content = Base64.getEncoder().encodeToString(content);
    String dataJson = "{\"content\":\"" + base64Content + "\"}";
    return CloudEventBuilder.v1()
        .withId(UUID.randomUUID().toString())
        .withSource(EVENT_SOURCE)
        .withType(eventType)
        .withSubject(subject)
        .withTime(OffsetDateTime.now())
        .withDataContentType(DATA_CONTENT_TYPE)
        .withData(DATA_CONTENT_TYPE, dataJson.getBytes(StandardCharsets.UTF_8))
        .build();
  }

  public static CloudEvent createUnpublishEvent(String eventType, String subject) {
    return CloudEventBuilder.v1()
        .withId(UUID.randomUUID().toString())
        .withSource(EVENT_SOURCE)
        .withType(eventType)
        .withSubject(subject)
        .withTime(OffsetDateTime.now())
        .build();
  }

}
