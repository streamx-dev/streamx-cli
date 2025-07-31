package dev.streamx.ingestion.payload;

import com.fasterxml.jackson.databind.JsonNode;
import dev.streamx.exception.GitHubActionException;
import dev.streamx.ingestion.IngestionPayload;
import dev.streamx.ingestion.IngestionPayloadJsonFactory;
import java.util.Collections;
import org.jboss.logging.Logger;

public class KeyPayload implements IngestionPayload {

  private static final Logger log = Logger.getLogger(KeyPayload.class);

  private final String action;

  private final String key;

  public KeyPayload(String action, String key) {
    this.action = action;
    this.key = key;
  }

  @Override
  public String getAction() {
    return action;
  }

  @Override
  public JsonNode resolve() throws GitHubActionException {
    JsonNode message = IngestionPayloadJsonFactory.createMessage(
        key,
        getAction(),
        null,
        Collections.emptyMap(),
        null
    );
    if (log.isDebugEnabled()) {
      log.debugf("Message: %s", message.toPrettyString());
    }
    return message;
  }
}
