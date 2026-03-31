package dev.streamx.ingestion;

import dev.streamx.exception.GitHubActionException;
import io.cloudevents.CloudEvent;

public interface IngestionPayload {

  CloudEvent resolve() throws GitHubActionException;

}
