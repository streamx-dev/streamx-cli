package dev.streamx.cli.command.ingestion;

import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import dev.streamx.cli.command.ingestion.publish.payload.PropertyCreatingJacksonJsonNodeJsonProvider;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import java.util.Set;

@Dependent
public class Config {

  private final JsonProvider jsonProvider;
  private final MappingProvider mappingProvider;

  @ApplicationScoped
  @PayloadProcessing
  ObjectMapper payloadProcessingObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.enable(Feature.ALLOW_SINGLE_QUOTES);

    return objectMapper;
  }

  Config(@PayloadProcessing ObjectMapper objectMapper) {
    jsonProvider = new PropertyCreatingJacksonJsonNodeJsonProvider(objectMapper);
    mappingProvider = new JacksonMappingProvider(objectMapper);
    configureDefaults();
  }


  private void configureDefaults() {
    Configuration.setDefaults(new Configuration.Defaults() {
      @Override
      public JsonProvider jsonProvider() {
        return jsonProvider;
      }

      @Override
      public Set<Option> options() {
        return Set.of();
      }

      @Override
      public MappingProvider mappingProvider() {
        return mappingProvider;
      }
    });
  }
}
