package dev.streamx.cli.settings;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jetbrains.annotations.NotNull;

@ApplicationScoped
public class SettingsStore {

  @ConfigProperty(name = "streamx.cli.settings.root-dir")
  String rootDir;

  @Inject
  @SettingsProcessing
  ObjectMapper objectMapper;

  public <T> Optional<T> retrieveSettings(String settingFile, Class<T> clazz) {
    try {
      Path path = resolveSettingFile(settingFile);
      if (Files.exists(path)) {
        T result = objectMapper.readValue(path.toFile(), clazz);
        return Optional.of(result);
      } else {
        return Optional.empty();
      }
    } catch (JsonProcessingException e) {
      // FIXME
      throw new RuntimeException(e);
    } catch (IOException e) {
      // FIXME
      throw new RuntimeException(e);
    }
  }

  public <T> void updateSettings(String settingFile, T settings) {
    Path path = resolveSettingFile(settingFile);
    try {
      Path rootPath = Path.of(rootDir);
      if (!Files.exists(rootPath)) {
        Files.createDirectory(rootPath);
      }
    } catch (IOException e) {
      // FIXME
      throw new RuntimeException(e);
    }
    try {
      objectMapper.writeValue(path.toFile(), settings);
    } catch (IOException e) {
      // FIXME
      throw new RuntimeException(e);
    }
  }

  @NotNull
  private Path resolveSettingFile(String settingFile) {
    return Path.of(rootDir).resolve(settingFile);
  }
}
