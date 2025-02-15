package dev.streamx.cli.util;

import java.nio.file.Path;

public class JsonSourceUtils {

  public static final String JSON_STRATEGY_PREFIX = "json://";

  private JsonSourceUtils() {
    // No instances
  }


  public static boolean applies(String rawSource) {
    return rawSource != null && rawSource.startsWith(JSON_STRATEGY_PREFIX);
  }

  public static Path resolve(String rawSource) {
    String source = rawSource.substring(JSON_STRATEGY_PREFIX.length());

    return Path.of(source);
  }
}
