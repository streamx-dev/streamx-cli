package dev.streamx.cli;

import dev.streamx.runner.validation.DockerEnvironmentValidator;

public class OsUtils {
  public static boolean isDockerAvailable() {
    try {
      new DockerEnvironmentValidator().validateDockerClient();
      return true;
    } catch (Exception e) {
      return false;
    }
  }
}
