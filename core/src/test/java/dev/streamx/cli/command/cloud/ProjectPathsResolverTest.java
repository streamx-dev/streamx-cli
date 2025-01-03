package dev.streamx.cli.command.cloud;

import static org.assertj.core.api.Assertions.assertThat;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

@QuarkusTest
class ProjectPathsResolverTest {

  @Inject
  ProjectPathsResolver cut;

  @Test
  void shouldReturnDefaultDeploymentPath() {
    Path deploymentPath = cut.resolveDeploymentPath(
        ProjectUtils.getResourcePath(Path.of("mesh.yaml")));
    assertThat(deploymentPath).endsWith(Path.of("deployment.yaml"));
  }

  @Test
  void shouldReturnCustomDeploymentPath() {
    Path deploymentPath = cut.resolveDeploymentPath(
        ProjectUtils.getResourcePath(Path.of("custom-name.yaml")));
    assertThat(deploymentPath).endsWith(Path.of("deployment.custom-name.yaml"));
  }

  @Test
  void shouldReturnSecretPath() {
    Path secretPath = cut.resolveSecretPath(ProjectUtils.getProjectPath(), "global.properties");
    assertThat(secretPath).endsWith(Path.of("project", "secrets", "global.properties"));
  }

  @Test
  void shouldReturnConfigPath() {
    Path secretPath = cut.resolveConfigPath(ProjectUtils.getProjectPath(), "global.properties");
    assertThat(secretPath).endsWith(Path.of("project", "configs", "global.properties"));
  }
}