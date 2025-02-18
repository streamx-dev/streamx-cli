package dev.streamx.cli.command.create;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.streamx.cli.exception.GitException;
import dev.streamx.cli.util.os.OsCommandStrategy;
import dev.streamx.cli.util.os.ProcessBuilder;
import io.quarkus.test.InjectMock;
import io.quarkus.test.component.QuarkusComponentTest;
import jakarta.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@QuarkusComponentTest
@ExtendWith(MockitoExtension.class)
class GitClientTest {

  @Inject
  GitClient gitClient;

  @InjectMock
  OsCommandStrategy strategy;

  @Mock
  ProcessBuilder processBuilder;

  @Mock
  Process process;

  @TempDir
  Path temp;

  @Test
  void shouldConfirmThatGitInstalled() throws IOException {
    // given
    mockProcess(0);
    String processOutput = "git version";
    mockProcessOutput(processOutput);

    // when
    boolean result = gitClient.isGitInstalled();

    // then
    assertThat(result).isTrue();
  }

  @Test
  void shouldDenyThatGitInstalled() throws IOException {
    // given
    mockProcess(127);
    String processOutput = "git version";
    mockProcessOutput(processOutput);

    // when
    boolean result = gitClient.isGitInstalled();

    // then
    assertThat(result).isFalse();
  }

  @Test
  void shouldValidateGitOutput() throws IOException {
    // given
    mockProcess(0);
    String processOutput = "gitOtherOutput";
    mockProcessOutput(processOutput);

    // when
    Throwable throwable = catchThrowable(() -> gitClient.validateGitInstalled());

    // then
    assertThat(throwable).isInstanceOf(GitException.class)
        .hasMessage("""
            Could not find a Git executable.
            
            Make sure that:
             * Git is installed,
             * Git is available on $PATH""");
  }

  @Test
  void shouldValidateGitInstalled() throws IOException {
    // given
    mockProcess(127);
    String processOutput = "git version";
    mockProcessOutput(processOutput);

    // when
    Throwable throwable = catchThrowable(() -> gitClient.validateGitInstalled());

    // then
    assertThat(throwable).isInstanceOf(GitException.class)
        .hasMessage("""
            Could not find a Git executable.
            
            Make sure that:
             * Git is installed,
             * Git is available on $PATH""");
  }

  @Test
  void shouldCloneRepository() throws IOException, InterruptedException {
    // given
    mockProcess(0);

    // when
    gitClient.clone("mockedUrl", "mockedOutputDir");

    // then
    verify(processBuilder).addEnv("GIT_TERMINAL_PROMPT", "0");
    verify(processBuilder).start();
    verify(process).waitFor();
    verify(process).exitValue();
  }

  @Test
  void shouldFailCloningRepository() throws IOException {
    // given
    mockProcess(1);

    // when
    GitException ex = catchThrowableOfType(() ->
        gitClient.clone("mockedUrl", "mockedOutputDir"),
        GitException.class);

    // then
    assertThat(ex).isNotNull();
    assertThat(ex.getProcess()).isEqualTo(process);
  }

  @Test
  void shouldRemoveDotGitDir() throws IOException {
    // given
    Path dotGitDirectory = temp.resolve(".git");
    Files.createDirectory(dotGitDirectory);
    Files.writeString(dotGitDirectory.resolve("config"),
        "someConfig", StandardCharsets.UTF_8);

    // when
    gitClient.removeGitMetadata(temp.toAbsolutePath().toString());

    // then
    assertThat(Files.exists(dotGitDirectory)).isFalse();
  }

  private void mockProcess(int exitValue) throws IOException {
    when(strategy.create(any())).thenReturn(processBuilder);
    when(processBuilder.start()).thenReturn(process);
    when(process.exitValue()).thenReturn(exitValue);
  }

  private void mockProcessOutput(String processOutput) {
    when(process.getInputStream()).thenReturn(new ByteArrayInputStream(processOutput.getBytes(
        StandardCharsets.UTF_8)));
  }
}
