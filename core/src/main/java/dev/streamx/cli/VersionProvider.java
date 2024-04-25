package dev.streamx.cli;

import java.net.URL;
import java.util.Properties;
import picocli.CommandLine.IVersionProvider;

public class VersionProvider implements IVersionProvider {

  @Override
  public String[] getVersion() throws Exception {
    URL url = getClass().getResource("/streamx-maven.properties");
    if (url == null) {
      return new String[] {"No version information included."};
    }
    Properties properties = new Properties();
    properties.load(url.openStream());
    if (properties.getProperty("streamx.cli.version") == null) {
      return new String[] {"No version information included."};
    }
    return new String[] {
        "streamx-cli version: " + properties.getProperty("streamx.cli.version")
    };
  }
}
