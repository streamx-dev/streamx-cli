package dev.streamx.cli.path;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SystemCurrentDirectoryProvider implements CurrentDirectoryProvider {

  @Override
  public String provide() {
    return "./";
  }
}
