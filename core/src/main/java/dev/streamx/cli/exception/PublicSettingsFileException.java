package dev.streamx.cli.exception;

public class PublicSettingsFileException extends SettingsFileException {

  public PublicSettingsFileException(String pathToSettings, Throwable cause) {
    super(pathToSettings, cause);
  }

  @Override
  public String getMessage() {
    return """
        Problem with settings file "%s".

        Detail: %s"""
        .formatted(getPathToSettings(), getCause().getMessage());
  }
}
