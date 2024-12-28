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
import java.util.stream.Stream;

@ApplicationScoped
public final class DataService {

  public static final String CONFIGS_DIRECTORY = "configs";
  public static final String SECRETS_DIRECTORY = "secrets";

  public enum ConfigType {
    DIR, FILE;

    public String getLabelValue() {
      return this.toString().toLowerCase();
    }
  }

  public Map<String, String> loadDataMapFromEnvFile(Path propertiesFilePath) {
    File propertiesFile = propertiesFilePath.toFile();
    if (!propertiesFile.exists() || !propertiesFile.isFile()) {
      throw new IllegalStateException("Path " + propertiesFilePath.normalize()
          + " provided in Mesh must be a valid properties file. Path was resolved to "
          + propertiesFilePath.toAbsolutePath().normalize() + ".");
    }
    Properties properties = new Properties();
    try (FileInputStream fis = new FileInputStream(propertiesFile)) {
      properties.load(fis);
      Map<String, String> data = new HashMap<>();
      for (Map.Entry<Object, Object> entry : properties.entrySet()) {
        data.put(entry.getKey().toString(), entry.getValue().toString());
      }
      return data;
    } catch (IOException e) {
      throw new IllegalStateException("Error reading properties file: " + propertiesFile, e);
    }
  }

  public Map<String, String> loadDataMapFromPath(Path path) {
    Map<String, String> data = new HashMap<>();
    try {
      if (Files.isRegularFile(path)) {
        String content = Files.readString(path);
        data.put(path.getFileName().toString(), content);
      } else if (Files.isDirectory(path)) {
        try (Stream<Path> walk = Files.walk(path, 1)) {
          walk
              .filter(Files::isRegularFile)
              .forEach(file -> {
                try {
                  String content = Files.readString(file);
                  String relativePath = path.relativize(file).toString();
                  data.put(relativePath, content);
                } catch (IOException e) {
                  throw new RuntimeException("Failed to read file: " + file, e);
                }
              });
        }
      } else {
        throw new IllegalArgumentException(
            "Path " + path.normalize()
                + " provided in Mesh must be a file or a directory. Path was resolved to "
                + path.toAbsolutePath().normalize() + ".");
      }
    } catch (IOException e) {
      throw new IllegalStateException("Could not convert " + path.normalize() + " to data", e);
    }

    return data;
  }

  public Path resolveSecretPath(Path projectPath, String sourcePath) {
    return resolveSourcePath(projectPath, SECRETS_DIRECTORY, sourcePath);
  }

  public Path resolveConfigPath(Path projectPath, String sourcePath) {
    return resolveSourcePath(projectPath, CONFIGS_DIRECTORY, sourcePath);
  }

  private Path resolveSourcePath(Path projectPath, String sourceDirectory, String sourcePath) {
    return projectPath.resolve(sourceDirectory).resolve(sourcePath);
  }

  public ConfigType getConfigType(Path dataSourcePath) {
    File dataSource = dataSourcePath.toFile();
    if (dataSource.isFile()) {
      return ConfigType.FILE;
    }
    if (dataSource.isDirectory()) {
      return ConfigType.DIR;
    }
    throw new IllegalStateException(
        "Config source " + dataSource + " provided in mesh should be file or directory.");
  }
}
