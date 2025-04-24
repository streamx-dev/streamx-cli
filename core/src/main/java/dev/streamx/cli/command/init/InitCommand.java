package dev.streamx.cli.command.init;

import static dev.streamx.cli.util.Output.printf;

import dev.streamx.cli.VersionProvider;
import dev.streamx.cli.command.init.project.template.ProjectTemplateSource;
import dev.streamx.cli.config.ArgumentConfigSource;
import dev.streamx.cli.exception.GitException;
import dev.streamx.cli.util.ExceptionUtils;
import dev.streamx.cli.util.FileUtils;
import jakarta.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.jboss.logging.Logger;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = InitCommand.COMMAND_NAME,
    mixinStandardHelpOptions = true,
    versionProvider = VersionProvider.class,
    description = "Initialize new StreamX project.")
public class InitCommand implements Runnable {

  public static final String COMMAND_NAME = "init";

  @Parameters(
      paramLabel = "outputDir", description = "path of newly created project",
      defaultValue = "streamx-sample-project", arity = "0..1"
  )
  void outputDir(String outputDir) {
    ArgumentConfigSource.registerValue(
        InitProjectConfig.STREAMX_INIT_PROJECT_TEMPLATE_OUTPUT_DIR, outputDir);
  }

  @Inject
  InitProjectConfig config;

  @Inject
  ProjectTemplateSource projectTemplateSource;

  @Inject
  Logger logger;

  @Inject
  GitClient gitClient;

  @Override
  public void run() {
    try {
      gitClient.validateGitInstalled();
      validateOutputDir();

      String outputDir = normalizeOutputDirPath(Path.of(config.outputDir()));
      printf("Initializing StreamX project...%n");
      gitClient.clone(projectTemplateSource.getRepoUrl(), outputDir);
      gitClient.removeGitMetadata(outputDir);
      printf("Project is ready in '%s'.%n", outputDir);
    } catch (GitException e) {
      logGitOutput(e);
      throw throwGenericException(e);
    } catch (Exception e) {
      throw throwGenericException(e);
    }
  }

  private void validateOutputDir() {
    Path path = Path.of(config.outputDir());
    String outputDir = normalizeOutputDirPath(path);
    logger.infov("Verifying output directory: {0}", outputDir);
    if (Files.exists(path)) {
      throw new RuntimeException(outputDir + " already exists");
    }
  }

  private static @NotNull String normalizeOutputDirPath(Path path) {
    Path outputDirPath = path.normalize().toAbsolutePath();
    return FileUtils.toString(outputDirPath);
  }

  private void logGitOutput(GitException e) {
    try {
      String stdOut = new String(e.getProcess().getInputStream().readAllBytes());
      logger.infov("Git command stdOut: \n{0}", stdOut, e);
      String stdErr = new String(e.getProcess().getErrorStream().readAllBytes());
      logger.errorv("Git command stdErr: \n{0}", stdErr, e);
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  private RuntimeException throwGenericException(Exception e) {
    return new RuntimeException(
        ExceptionUtils.appendLogSuggestion(
            "Unable to initialize new project.\n"
                + "\n"
                + "Details:\n"
                + e.getMessage()), e);
  }
}
