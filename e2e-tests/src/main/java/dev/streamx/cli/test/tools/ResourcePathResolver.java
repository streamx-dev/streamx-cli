package dev.streamx.cli.test.tools;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ResourcePathResolver {

  public static String absolutePath(String fileName) {
    ClassLoader classLoader = ResourcePathResolver.class.getClassLoader();
    Path path;
    try {
      path = Paths.get(classLoader.getResource(fileName).toURI());
    } catch (URISyntaxException e) {
      throw new RuntimeException("Can not get file path", e);
    }
    return path.toAbsolutePath().toString();
  }
}
