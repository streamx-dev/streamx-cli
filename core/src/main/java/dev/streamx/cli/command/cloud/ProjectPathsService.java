package dev.streamx.cli.command.cloud;

import static dev.streamx.cli.command.run.MeshDefinitionResolver.MESH_YAML;

import jakarta.enterprise.context.ApplicationScoped;
import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;

@ApplicationScoped
public class ProjectPathsService {

  public static final String CONFIGS_DIRECTORY = "configs";
  public static final String SECRETS_DIRECTORY = "secrets";
  protected static final String YAML_EXT = ".yaml";
  static final String DEPLOYMENT = "deployment";
  static final String DEPLOYMENT_FILE_NAME = DEPLOYMENT + YAML_EXT;

  @NotNull
  public Path resolveDeploymentPath(Path meshPath) {
    String meshFileName = meshPath.getFileName().toString();
    String deploymentFileName = DEPLOYMENT_FILE_NAME;
    if (!MESH_YAML.equals(meshFileName)) {
      deploymentFileName = DEPLOYMENT + "." + meshFileName;
    }
    return meshPath.getParent().resolve(deploymentFileName);
  }

  public Path resolveSecretPath(Path projectPath, String sourcePath) {
    return resolveSourcePath(projectPath, SECRETS_DIRECTORY, sourcePath);
  }

  public Path resolveConfigPath(Path projectPath, String sourcePath) {
    return resolveSourcePath(projectPath, CONFIGS_DIRECTORY, sourcePath);
  }

  private Path resolveSourcePath(Path projectPath, String sourceDirectory, String sourcePath) {
    return projectPath.resolve(sourceDirectory).resolve(sourcePath);
  }
}
