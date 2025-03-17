package dev.streamx.cli.command.ingestion;

import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Singleton;

@Dependent
public class PayloadProcessingConfig {

  @Singleton
  @PayloadProcessing
  ObjectMapper payloadProcessingObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.enable(Feature.ALLOW_SINGLE_QUOTES);

    return objectMapper;
  }
}
