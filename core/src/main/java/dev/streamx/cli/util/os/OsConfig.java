package dev.streamx.cli.util.os;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Dependent
public class OsConfig {
  @ApplicationScoped
  public OsCommandStrategy strategy(@ConfigProperty(name = "os.name") String osName) {
    if (osName.toLowerCase().contains("win")) {
      return new CmdCommandStrategy();
    }
    return new ShellCommandStrategy();
  }
}
