package dev.streamx.cli.command.cloud;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class ProjectUtils {

  private static final String PROJECT_PATH = "project/";

  public static Path getMeshPath(String name) {
    try {
      return Paths.get(ProjectUtils.class.getResource(PROJECT_PATH + name).toURI());
    } catch (URISyntaxException e) {
      throw new RuntimeException("Could not map mesh resource to URI", e);
    }
  }

  public static String getResource(String resourceName) throws IOException {
    try (InputStream is = ServiceMeshServiceTest.class.getResourceAsStream(
        PROJECT_PATH + resourceName)) {
      return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    }
  }
}
