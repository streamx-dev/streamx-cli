package dev.streamx.cli.command.cloud.deploy;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Stream;

public final class DataResolver {

  static Map<String, String> loadDataMapFromEnvFile(Path propertiesFilePath) {
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

  static Map<String, String> loadDataMapFromPath(Path path) {
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
}
