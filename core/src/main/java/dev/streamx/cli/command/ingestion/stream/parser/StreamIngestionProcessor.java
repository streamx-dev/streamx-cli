package dev.streamx.cli.command.ingestion.stream.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.streamx.clients.ingestion.exceptions.StreamxClientException;
import java.io.IOException;

@FunctionalInterface
public interface StreamIngestionProcessor {

  void apply(JsonNode cloudEventNode) throws IOException, StreamxClientException;
}
