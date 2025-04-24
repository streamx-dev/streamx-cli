package dev.streamx.cli;

import dev.streamx.runner.validation.DockerEnvironmentValidator;
import org.apache.commons.lang3.StringEscapeUtils;

public class OsUtils {
  public static final String ESCAPED_LINE_SEPARATOR =
      StringEscapeUtils.escapeJson(System.lineSeparator());

  public static boolean isDockerAvailable() {
    try {
      new DockerEnvironmentValidator().validateDockerClient();
      return true;
    } catch (Exception e) {
      return false;
    }
  }
}
