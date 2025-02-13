package dev.streamx.cli.command.ingestion.batch.resolver;

import com.fasterxml.jackson.databind.JsonNode;
import dev.streamx.cli.command.ingestion.batch.EventSourceDescriptor;
import java.util.Map;

public interface BatchPayloadResolver {

  JsonNode createPayload(EventSourceDescriptor currentDescriptor, Map<String, String> variables);
}
