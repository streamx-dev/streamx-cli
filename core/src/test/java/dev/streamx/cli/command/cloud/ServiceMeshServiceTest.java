package dev.streamx.cli.command.cloud;

import static dev.streamx.cli.command.cloud.ServiceMeshService.DEPLOYMENT;
import static dev.streamx.cli.command.cloud.ServiceMeshService.DEPLOYMENT_FILE_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.streamx.cli.command.cloud.ServiceMeshService.ConfigSourcesPaths;
import dev.streamx.mesh.model.AbstractContainer;
import dev.streamx.operator.crd.ServiceMesh;
import dev.streamx.operator.crd.deployment.ServiceMeshDeploymentConfig;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

@QuarkusTest
class ServiceMeshServiceTest {

  @Inject
  ServiceMeshService cut;

  @Test
  void shouldReturnServiceMeshWithDefaultDeploymentName() throws IOException {
    ServiceMesh serviceMesh = getServiceMesh("mesh.yaml");
    assertNotNull(serviceMesh);
    String deploymentConfigYaml = mapDeploymentConfigToYaml(serviceMesh);
    String expected = ProjectUtils.getResource(DEPLOYMENT_FILE_NAME);
    assertEquals(expected, deploymentConfigYaml);
  }

  @Test
  void shouldReturnServiceMeshWithCustomDeploymentName() throws IOException {
    ServiceMesh serviceMesh = getServiceMesh("custom-name.yaml");
    assertNotNull(serviceMesh);
    String deploymentConfigYaml = mapDeploymentConfigToYaml(serviceMesh);
    String expected = ProjectUtils.getResource(DEPLOYMENT + ".custom-name.yaml");
    assertEquals(expected, deploymentConfigYaml);
  }

  @Test
  void shouldReturnServiceMeshWithoutDeployment() {
    ServiceMesh serviceMesh = getServiceMesh("nodeployment.yaml");
    assertNotNull(serviceMesh);
    assertNull(serviceMesh.getSpec().getDeploymentConfig());
  }

  @Test
  void shouldReturnMessageAboutInvalidMeshPath() {
    assertThrowsExactly(RuntimeException.class,
        () -> cut.getServiceMesh(Path.of("nonexisting.mesh.yaml")),
        "File with provided path 'nonexisting.mesh.yaml' does not exist.");
  }

  @Test
  void shouldReturnAllConfigurableContainers() {
    ServiceMesh serviceMesh = getServiceMesh("with-configs.yaml");
    List<AbstractContainer> containers = cut.getContainers(serviceMesh);
    assertEquals(4, containers.size());
  }

  @Test
  void shouldReturnAllConfigSourcesPaths() {
    ServiceMesh serviceMesh = getServiceMesh("with-configs.yaml");
    ConfigSourcesPaths configSourcesPaths = cut.getConfigSourcesPaths(serviceMesh);
    Set<String> expectedEnvsPaths = Set.of(
        "global.properties",
        "ingestion/rest.properties",
        "processing/relay.properties",
        "delivery/wds.properties",
        "delivery/wds/nginx.properties",
        "shared.properties"
    );
    assertThat(configSourcesPaths.envConfigsPaths()).containsExactlyInAnyOrderElementsOf(
        expectedEnvsPaths);
    assertThat(configSourcesPaths.envSecretsPaths()).containsExactlyInAnyOrderElementsOf(
        expectedEnvsPaths);
    Set<String> expectedVolumesPaths = Set.of(
        "ingestion/rest/file.txt",
        "ingestion/rest/subdir/file.txt",
        "processing/relay/file.txt",
        "processing/relay/subdir/file.txt",
        "delivery/wds/file.txt",
        "delivery/wds/subdir/file.txt",
        "delivery/wds/dir",
        "delivery/wds/nginx/file.txt",
        "delivery/wds/nginx/subdir/file.txt",
        "shared"
    );
    assertThat(configSourcesPaths.volumesConfigsPaths()).containsExactlyInAnyOrderElementsOf(
        expectedVolumesPaths);
    assertThat(configSourcesPaths.volumesSecretsPaths()).containsExactlyInAnyOrderElementsOf(
        expectedVolumesPaths);
  }

  @NotNull
  private ServiceMesh getServiceMesh(String meshName) {
    Path meshPath = ProjectUtils.getMeshPath(meshName);
    return cut.getServiceMesh(meshPath);
  }

  @NotNull
  private String mapDeploymentConfigToYaml(ServiceMesh serviceMesh) throws JsonProcessingException {
    ServiceMeshDeploymentConfig deploymentConfig = serviceMesh.getSpec().getDeploymentConfig();
    return cut.objectMapper.writeValueAsString(deploymentConfig);
  }
}