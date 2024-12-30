package dev.streamx.cli;

import dev.streamx.cli.util.StreamxMavenPropertiesUtils;
import picocli.CommandLine.IVersionProvider;

public class VersionProvider implements IVersionProvider {

  @Override
  public String[] getVersion() {
    var streamxVersion = StreamxMavenPropertiesUtils.getStreamxCliVersion();
    if (streamxVersion == null) {
      return new String[]{"No version information included."};
    }
    return new String[]{
        "streamx-cli version: " + streamxVersion
    };
  }
}
