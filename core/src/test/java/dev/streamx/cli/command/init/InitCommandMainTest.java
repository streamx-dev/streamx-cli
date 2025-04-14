package dev.streamx.cli.command.init;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import dev.streamx.cli.command.init.InitCommandMainTest.SampleLocalRepositoryResource;
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
class InitCommandMainTest {

  public static final String PN_SAMPLE_GIT_REPOSITORY = "sampleGitRepository";
  public static final String GIT_NOT_INSTALLED = "gitNotInstalled";
  private static Path localRepoDirectory;

  @TempDir
  Path outputDirectory;

  @Test
  void shouldCreateProject(QuarkusMainLauncher launcher) {
    // given
    assumeGitIsInstalled();

    // when
    String outputDir = resolveOutputDir();
    LaunchResult result = launcher.launch("init", outputDir);

    // then
    assertThat(result.exitCode()).isEqualTo(0);
    assertThat(result.getOutput()).contains("Initializing StreamX project...");
    assertThat(result.getOutput()).contains("Project is ready in");
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

  private static void assumeGitIsInstalled() {
    assumeTrue(GitClientUtils.isGitInstalled(getOsCommandStrategy()),
        "Git not installed.");
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
      OsCommandStrategy commandStrategy = getOsCommandStrategy();

      if (GitClientUtils.isGitInstalled(commandStrategy)) {
        String sampleGitRepository = initializeLocalGitRepository();
        prepareSampleLocalRepository();

        System.setProperty(PN_SAMPLE_GIT_REPOSITORY, sampleGitRepository);
        return Map.of("streamx.cli.init.project.template.repo-url", sampleGitRepository);
      } else {
        return Map.of("streamx.cli.init.project.template.repo-url", GIT_NOT_INSTALLED);
      }
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
        exec("git config user.name \"test\"", cloneDirFile);
        exec("git config user.email \"test@test.dev\"", cloneDirFile);
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
