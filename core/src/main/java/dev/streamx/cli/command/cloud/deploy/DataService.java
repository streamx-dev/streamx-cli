package dev.streamx.cli.command.cloud.deploy;

import jakarta.enterprise.context.ApplicationScoped;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@ApplicationScoped
public final class DataService {

  private static final Pattern validKeyPattern = Pattern.compile("[-._a-zA-Z0-9]+");

  public Map<String, String> loadDataFromProperties(Path propertiesFilePath) {
    File propertiesFile = propertiesFilePath.toFile();
    if (!propertiesFile.exists() || !propertiesFile.isFile()) {
      throw new IllegalStateException("Path " + propertiesFilePath.normalize()
          + " provided in Mesh must be a valid properties file.");
    }
    Properties properties = new Properties();
    try (FileInputStream fis = new FileInputStream(propertiesFile)) {
      properties.load(fis);
      Map<String, String> data = new HashMap<>();
      for (Map.Entry<Object, Object> entry : properties.entrySet()) {
        String propertyKey = entry.getKey().toString();
        validatePropertyKey(propertiesFilePath.toString(), propertyKey);
        data.put(propertyKey, entry.getValue().toString());
      }
      return data;
    } catch (IOException e) {
      throw new IllegalStateException("Error reading properties file: " + propertiesFile, e);
    }
  }

  public Map<String, String> loadDataFromFiles(Path path) {
    Map<String, String> data = new HashMap<>();
    try {
      if (Files.isRegularFile(path)) {
        String content = Files.readString(path);
        String fileName = path.getFileName().toString();
        validateFileName(path.toString(), fileName);
        data.put(fileName, content);
      } else if (Files.isDirectory(path)) {
        try (Stream<Path> walk = Files.walk(path, 1)) {
          walk
              .filter(Files::isRegularFile)
              .forEach(file -> {
                try {
                  String content = Files.readString(file);
                  String fileName = file.getFileName().toString();
                  validateFileName(path.toString(), fileName);
                  data.put(fileName, content);
                } catch (IOException e) {
                  throw new RuntimeException("Failed to read data file: " + file.toAbsolutePath(),
                      e);
                }
              });
        }
      } else {
        throw new IllegalArgumentException(
            "Path " + path.normalize() + " provided in Mesh must be a file or a directory.");
      }
    } catch (IOException e) {
      throw new IllegalStateException("Failed to convert " + path.normalize() + " to data", e);
    }

    return data;
  }

  private void validateFileName(String configPath, String fileName) {
    if (isConfigDataKeyInvalid(fileName)) {
      throw new IllegalArgumentException(
          "Invalid file name: " + fileName + " in volumesFrom: " + configPath
              + ". Valid file name must consist of alphanumeric characters, '-', '_' or '.'.");
    }
  }

  private void validatePropertyKey(String configPath, String key) {
    if (isConfigDataKeyInvalid(key)) {
      throw new IllegalArgumentException(
          "Invalid properties key: " + key + " in environmentFrom: " + configPath
              + ". Valid property key must consist of alphanumeric characters, '-', '_' or '.'.");
    }
  }

  private boolean isConfigDataKeyInvalid(String key) {
    Matcher matcher = validKeyPattern.matcher(key);
    return !matcher.matches();
  }
}
