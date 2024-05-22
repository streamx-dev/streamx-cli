package dev.streamx.cli.exception;

public class PublicSettingsFileException extends SettingsFileException {

  public PublicSettingsFileException(String pathToSettings, Throwable cause) {
    super(pathToSettings, cause);
  }

  @Override
  public String getMessage() {
    return """
        Problem with setting file "%s".

        Detail: %s"""
        .formatted(getPathToSettings(), getCause().getMessage());
  }
}
