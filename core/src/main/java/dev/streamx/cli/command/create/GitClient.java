package dev.streamx.cli.command.create;

import dev.streamx.cli.exception.GitException;
import dev.streamx.cli.util.os.OsCommandStrategy;
import dev.streamx.cli.util.os.ProcessBuilder;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import org.apache.commons.io.FileUtils;
import org.jboss.logging.Logger;

@ApplicationScoped
public class GitClient {

  @Inject
  OsCommandStrategy strategy;

  @Inject
  Logger logger;

  public boolean isGitInstalled() {
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

  public void clone(String cloneUrl, String outputDir) {
    try {
      ProcessBuilder processBuilder = strategy.create(
          "git clone %s %s".formatted(cloneUrl, outputDir));

      processBuilder.addEnv("GIT_TERMINAL_PROMPT", "0");

      Process process = processBuilder.start();
      process.waitFor();

      if (process.exitValue() != 0) {
        throw GitException.gitCloneException(process);
      }
    } catch (IOException | InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  public void removeGitMetadata(String directoryPath) throws IOException {
    File directory = Path.of(directoryPath).resolve(".git").toFile();
    FileUtils.deleteDirectory(directory);
  }

  public void validateGitInstalled() {
    logger.info("Verifying git installation...");
    try {
      Process process = strategy.create("git --version").start();
      process.waitFor();
      byte[] stdOutBytes = process.getInputStream().readAllBytes();

      if (process.exitValue() != 0) {
        logger.info("Git not installed...");
        throw GitException.gitNotInstalledException(process);
      } else if (!new String(stdOutBytes).startsWith("git version")) {
        logger.info("Git not installed...");
        throw GitException.gitNotInstalledException(process);
      }
    } catch (IOException | InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}
