package dev.streamx.cli.settings;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;

@Dependent
public class SettingsConfig {

  @ApplicationScoped
  @SettingProcessing
  ObjectMapper settingProcessingObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();

    return objectMapper;
  }
}
