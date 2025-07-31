package dev.streamx.ingestion;

import com.fasterxml.jackson.databind.JsonNode;
import dev.streamx.exception.GitHubActionException;

public interface IngestionPayload {

  String TYPE_KEY = "sx:type";

  String getAction();

  JsonNode resolve() throws GitHubActionException;

}
