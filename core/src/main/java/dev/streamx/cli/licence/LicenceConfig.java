package dev.streamx.cli.licence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;

@Dependent
public class LicenceConfig {

  @ApplicationScoped
  @LicenceProcessing
  ObjectMapper licencesProcessingObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());

    return objectMapper;
  }
}
