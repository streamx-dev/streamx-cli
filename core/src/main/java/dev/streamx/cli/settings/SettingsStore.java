package dev.streamx.cli.settings;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.streamx.cli.exception.SettingsFileException;
import dev.streamx.cli.util.FileUtils;
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
    Path path = resolveSettingFile(settingFile);
    try {
      if (Files.exists(path)) {
        T result = objectMapper.readValue(path.toFile(), clazz);
        return Optional.of(result);
      } else {
        return Optional.empty();
      }
    } catch (IOException e) {
      throw new SettingsFileException(FileUtils.toString(path), e);
    }
  }

  public <T> void updateSettings(String settingFile, T settings) {
    Path path = resolveSettingFile(settingFile);
    try {
      Path rootPath = Path.of(rootDir);
      if (!Files.exists(rootPath)) {
        Files.createDirectories(rootPath);
      }
    } catch (IOException e) {
      throw new SettingsFileException(FileUtils.toString(path), e);
    }

    try {
      objectMapper.writeValue(path.toFile(), settings);
    } catch (IOException e) {
      throw new SettingsFileException(FileUtils.toString(path), e);
    }
  }

  @NotNull
  private Path resolveSettingFile(String settingFile) {
    return Path.of(rootDir).resolve(settingFile);
  }
}
