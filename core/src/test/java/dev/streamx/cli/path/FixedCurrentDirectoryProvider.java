package dev.streamx.cli.path;

import java.nio.file.Path;

public class FixedCurrentDirectoryProvider implements CurrentDirectoryProvider {

  private final Path path;

  public FixedCurrentDirectoryProvider(Path path) {
    this.path = path;
  }

  @Override
  public String provide() {
    return path.toAbsolutePath().normalize().toString();
  }
}
