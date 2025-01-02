package dev.streamx.cli.command.cloud;

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
  void shouldThrowExceptionForEmptyMeshFile() {
    Path meshPath = ProjectUtils.getResourcePath(Path.of("empty-mesh.yaml"));
    RuntimeException runtimeException = assertThrowsExactly(RuntimeException.class,
        () -> cut.getServiceMesh(meshPath));
    assertThat(runtimeException.getMessage()).isEqualTo(
        "Mesh file with provided path '" + meshPath + "' is empty.");
  }

  @Test
  void shouldReturnServiceMeshWithDefaultDeploymentName() throws IOException {
    ServiceMesh serviceMesh = getServiceMesh("mesh.yaml");
    assertNotNull(serviceMesh);
    String deploymentConfigYaml = mapDeploymentConfigToYaml(serviceMesh);
    String expected = ProjectUtils.getResource("deployment.yaml");
    assertEquals(expected, deploymentConfigYaml);
  }

  @Test
  void shouldReturnServiceMeshWithCustomDeploymentName() throws IOException {
    ServiceMesh serviceMesh = getServiceMesh("custom-name.yaml");
    assertNotNull(serviceMesh);
    String deploymentConfigYaml = mapDeploymentConfigToYaml(serviceMesh);
    String expected = ProjectUtils.getResource("deployment.custom-name.yaml");
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
    RuntimeException runtimeException = assertThrowsExactly(RuntimeException.class,
        () -> cut.getServiceMesh(Path.of("nonexisting.mesh.yaml")));
    assertEquals("File with provided path 'nonexisting.mesh.yaml' does not exist.",
        runtimeException.getMessage());
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
    assertThat(configSourcesPaths.configEnvPaths()).containsExactlyInAnyOrderElementsOf(
        expectedEnvsPaths);
    assertThat(configSourcesPaths.secretEnvPaths()).containsExactlyInAnyOrderElementsOf(
        expectedEnvsPaths);
    Set<String> expectedVolumesPaths = Set.of(
        "ingestion/rest/file.txt",
        "processing/relay/file.txt",
        "delivery/wds/file.txt",
        "delivery/wds/dir",
        "delivery/wds/nginx/file.txt",
        "shared"
    );
    assertThat(configSourcesPaths.configVolumePaths()).containsExactlyInAnyOrderElementsOf(
        expectedVolumesPaths);
    assertThat(configSourcesPaths.secretVolumePaths()).containsExactlyInAnyOrderElementsOf(
        expectedVolumesPaths);
  }

  @Test
  void shouldMapEmptyDeploymentFileToNull() {
    Path meshPath = ProjectUtils.getResourcePath(Path.of("empty-deployment.yaml"));
    ServiceMesh serviceMesh = cut.getServiceMesh(meshPath);
    assertThat(serviceMesh.getSpec().getDeploymentConfig()).isNull();
  }

  @NotNull
  private ServiceMesh getServiceMesh(String meshName) {
    Path meshPath = ProjectUtils.getResourcePath(Path.of(meshName));
    return cut.getServiceMesh(meshPath);
  }

  @NotNull
  private String mapDeploymentConfigToYaml(ServiceMesh serviceMesh) throws JsonProcessingException {
    ServiceMeshDeploymentConfig deploymentConfig = serviceMesh.getSpec().getDeploymentConfig();
    return ServiceMeshService.objectMapper.writeValueAsString(deploymentConfig);
  }
}