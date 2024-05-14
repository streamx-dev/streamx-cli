package dev.streamx.cli.ingestion.publish.payload.typed;

import com.fasterxml.jackson.databind.JsonNode;

public record TypedPayload(JsonNode jsonNode) {

}
