package dev.streamx.cli.command.create;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import dev.streamx.cli.command.create.project.template.ProjectTemplateSource;
import dev.streamx.cli.exception.GitException;
import io.quarkus.test.InjectMock;
import io.quarkus.test.component.QuarkusComponentTest;
import io.quarkus.test.component.TestConfigProperty;
import jakarta.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@QuarkusComponentTest(
    annotationsTransformers = dev.streamx.cli.command.util.CommandTransformer.class
)
@TestConfigProperty(
    key = CreateProjectConfig.STREAMX_CREATE_PROJECT_TEMPLATE_OUTPUT_DIR,
    value = CreateCommandTest.OUTPUT_DIR
)
@TestConfigProperty(
    key = "quarkus.log.file.path",
    value = "/log/path"
)
@ExtendWith(MockitoExtension.class)
class CreateCommandTest {

  public static final String OUTPUT_DIR = "outputDir";
  public static final String REPO_URL = "repoUrl";
  public static final String NORMALIZED_OUTPUT_DIR = Path.of(OUTPUT_DIR)
      .normalize().toAbsolutePath().toString();

  @Inject
  CreateCommand createCommand;

  @InjectMock
  GitClient gitClient;

  @InjectMock
  ProjectTemplateSource projectTemplateSource;

  @Mock
  Process process;

  @AfterEach
  void cleanup() throws IOException {
    FileUtils.deleteDirectory(Path.of(OUTPUT_DIR).toFile());
  }

  @Test
  void shouldValidateGitInstallation() {
    // given
    mockProcessOutput("output");
    mockProcessErrorOutput("error");
    doThrow(GitException.gitCloneException(process)).when(gitClient).validateGitInstalled();

    // when
    Throwable throwable = catchThrowable(() -> createCommand.run());

    // then
    assertThat(throwable).isInstanceOf(RuntimeException.class)
        .hasMessage("""
            Unable create project template.
            
            Details:
            git clone failed.
            
            Full logs can be found in /log/path""");
    verify(gitClient).validateGitInstalled();
    verifyNoMoreInteractions(gitClient);
  }

  @Test
  void shouldValidateOutputDirExists() throws IOException {
    // given
    Files.createDirectories(Path.of(OUTPUT_DIR));

    // when
    Throwable throwable = catchThrowable(() -> createCommand.run());

    // then
    assertThat(throwable).isInstanceOf(RuntimeException.class)
        .hasMessage("""
            Unable create project template.

            Details:
            %s already exists

            Full logs can be found in /log/path""".formatted(NORMALIZED_OUTPUT_DIR));
    verify(gitClient).validateGitInstalled();
    verifyNoMoreInteractions(gitClient);
  }

  @Test
  void shouldExecuteCommandSuccessfully() throws IOException {
    // given
    when(projectTemplateSource.getRepoUrl()).thenReturn(REPO_URL);

    // when
    createCommand.run();

    // then
    verify(gitClient).validateGitInstalled();
    verify(gitClient).clone(REPO_URL, NORMALIZED_OUTPUT_DIR);
    verify(gitClient).removeGitMetadata(NORMALIZED_OUTPUT_DIR);
  }

  private void mockProcessOutput(String processOutput) {
    when(process.getInputStream()).thenReturn(new ByteArrayInputStream(processOutput.getBytes(
        StandardCharsets.UTF_8)));
  }

  private void mockProcessErrorOutput(String processOutput) {
    when(process.getErrorStream()).thenReturn(new ByteArrayInputStream(processOutput.getBytes(
        StandardCharsets.UTF_8)));
  }
}
