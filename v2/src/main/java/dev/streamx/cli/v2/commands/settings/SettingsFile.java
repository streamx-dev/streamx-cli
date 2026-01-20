package dev.streamx.cli.v2.commands.settings;

import static dev.streamx.cli.v2.i18n.MessageProvider.msg;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;

public class SettingsFile {
  public static URL getUrl() throws RuntimeException {
    String rootDir = System.getProperty("user.home");
    String dotStreamxConfig = rootDir + "/.streamx/config";

    Path pathToDir = Path.of(dotStreamxConfig);
    Path pathToFile = pathToDir.resolve("application.properties");

    try {
      return pathToFile.toUri().toURL();
    } catch (MalformedURLException e) {
      throw new RuntimeException(msg.unableToGetSettingsFilePath(), e);
    }
  }
}
