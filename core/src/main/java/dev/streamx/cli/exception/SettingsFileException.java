package dev.streamx.cli.exception;

public class SettingsFileException extends RuntimeException {

  private final String pathToSettings;

  public SettingsFileException(String pathToSettings, Throwable cause) {
    super(cause);
    this.pathToSettings = pathToSettings;
  }

  public String getPathToSettings() {
    return pathToSettings;
  }
}
