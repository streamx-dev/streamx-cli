package dev.streamx.cli.util;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;

public class StreamxMavenPropertiesUtils {

  private static final Properties PROPERTIES;

  static {
    URL url = StreamxMavenPropertiesUtils.class.getResource("/streamx-maven.properties");
    Properties properties = null;
    if (url != null) {
      try {
        var loadedProperties = new Properties();
        loadedProperties.load(url.openStream());

        properties = loadedProperties;
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    PROPERTIES = properties;
  }

  public static String getStreamxCliVersion() {
    return getProperty("streamx.cli.version");
  }

  public static String getMeshManagerImage() {
    return getProperty("streamx.manager.mesh-manager.image");
  }

  private static String getProperty(String key) {
    return PROPERTIES.getProperty(key);
  }

}
