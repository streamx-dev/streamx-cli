package dev.streamx.cli.command.create;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import dev.streamx.cli.command.create.CreateCommandMainTest.SampleLocalRepositoryResource;
import dev.streamx.cli.util.os.CmdCommandStrategy;
import dev.streamx.cli.util.os.OsCommandStrategy;
import dev.streamx.cli.util.os.ProcessBuilder;
import dev.streamx.cli.util.os.ShellCommandStrategy;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import io.quarkus.test.common.WithTestResource;
import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainLauncher;
import io.quarkus.test.junit.main.QuarkusMainTest;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@QuarkusMainTest
@WithTestResource(SampleLocalRepositoryResource.class)
class CreateCommandMainTest {

  public static final String PN_SAMPLE_GIT_REPOSITORY = "sampleGitRepository";
  private static Path localRepoDirectory;

  @TempDir
  Path outputDirectory;

  @Test
  void shouldCreateProject(QuarkusMainLauncher launcher) {
    // given
    assumeGitIsInstalled();

    // when
    String outputDir = resolveOutputDir();
    LaunchResult result = launcher.launch("create", outputDir);

    // then
    assertThat(result.exitCode()).isEqualTo(0);
    assertThat(result.getOutput()).contains("Creating StreamX project template");
    assertThat(result.getOutput()).contains("Project created");
    assertThat(outputDirectory.toFile().exists()).isTrue();
    assertThat(result.getErrorOutput()).isEmpty();
  }

  private String resolveOutputDir() {
    return outputDirectory
        .resolve("clonedRepo")
        .toAbsolutePath()
        .normalize()
        .toString();
  }

  private void assumeGitIsInstalled() {
    GitClient gitClient = new GitClient();
    gitClient.strategy = getOsCommandStrategy();

    assumeTrue(gitClient.isGitInstalled(), "Git not installed.");
  }

  private static OsCommandStrategy getOsCommandStrategy() {
    String osName = System.getProperty("os.name").toLowerCase();
    if (osName.contains("win")) {
      return new CmdCommandStrategy();
    }
    return new ShellCommandStrategy();
  }

  public static class SampleLocalRepositoryResource implements QuarkusTestResourceLifecycleManager {

    @Override
    public Map<String, String> start() {
      String sampleGitRepository = initializeLocalGitRepository();
      prepareSampleLocalRepository();

      System.setProperty(PN_SAMPLE_GIT_REPOSITORY, sampleGitRepository);
      return Map.of("streamx.cli.create.project.template.repo-url", sampleGitRepository);
    }

    private static String initializeLocalGitRepository() {
      try {
        localRepoDirectory = Files.createTempDirectory("test-repository");
        return localRepoDirectory.normalize().toAbsolutePath().toString();

      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    private void prepareSampleLocalRepository() {
      try {
        File cloneDirFile = localRepoDirectory.toFile();
        String command = "git init " + localRepoDirectory.toAbsolutePath().normalize();
        exec(command, cloneDirFile);
        Files.writeString(localRepoDirectory.resolve("file.txt"), "test");
        exec("git add -A && git commit -m 'testCommit'", cloneDirFile);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    private void exec(String command, File file) throws IOException, InterruptedException {
      ProcessBuilder builder = getOsCommandStrategy().create(command);
      builder.directory(file);
      Process process = builder.start();
      int status = process.waitFor();
      if (status != 0) {
        fail("Command %s exited with status %d", command, status);
      }
    }

    @Override
    public void stop() {

    }
  }
}
