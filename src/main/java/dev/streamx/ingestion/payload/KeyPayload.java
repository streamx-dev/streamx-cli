package dev.streamx.ingestion.payload;

import dev.streamx.exception.GitHubActionException;
import dev.streamx.ingestion.IngestionPayload;
import io.cloudevents.CloudEvent;
import org.jboss.logging.Logger;

public class KeyPayload implements IngestionPayload {

  private static final Logger log = Logger.getLogger(KeyPayload.class);

  private final String eventType;
  private final String key;

  public KeyPayload(String eventType, String key) {
    this.eventType = eventType;
    this.key = key;
  }

  @Override
  public CloudEvent resolve() throws GitHubActionException {
    CloudEvent event = CloudEventFactory.createUnpublishEvent(eventType, key);
    if (log.isDebugEnabled()) {
      log.debugf("CloudEvent: type=%s, subject=%s", event.getType(), event.getSubject());
    }
    return event;
  }
}
