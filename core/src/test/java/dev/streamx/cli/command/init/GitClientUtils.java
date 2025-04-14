package dev.streamx.cli.command.init;

import dev.streamx.cli.util.os.OsCommandStrategy;
import java.io.IOException;

public class GitClientUtils {

  public static boolean isGitInstalled(OsCommandStrategy strategy) {
    try {
      Process process = strategy.create("git --version").start();
      process.waitFor();
      byte[] stdOutBytes = process.getInputStream().readAllBytes();

      if (process.exitValue() != 0) {
        return false;
      }
      return new String(stdOutBytes).startsWith("git version");
    } catch (IOException | InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}
