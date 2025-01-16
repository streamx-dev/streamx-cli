package dev.streamx.cli.command.cloud;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.streamx.cli.interpolation.Interpolating;
import dev.streamx.cli.util.ExceptionUtils;
import dev.streamx.mesh.model.AbstractContainer;
import dev.streamx.mesh.model.AbstractFromSource;
import dev.streamx.mesh.model.DeliveryService;
import dev.streamx.mesh.model.EnvironmentFrom;
import dev.streamx.mesh.model.VolumesFrom;
import dev.streamx.operator.crd.ServiceMesh;
import dev.streamx.operator.crd.ServiceMeshSpec;
import dev.streamx.operator.crd.deployment.ServiceMeshDeploymentConfig;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ApplicationScoped
public class ServiceMeshResolver {

  public static final String SERVICE_MESH_NAME = "sx";
  @Inject
  @Interpolating
  ObjectMapper objectMapper;

  @Inject
  ProjectPathsResolver projectPathsResolver;

  @NotNull
  public ServiceMesh resolveMesh(Path meshPath) {
    File meshPathFile = meshPath.toFile();
    if (!meshPathFile.exists()) {
      throw new RuntimeException("Mesh file with provided path '" + meshPath + "' does not exist.");
    }
    if (meshPathFile.length() < 1) {
      throw new RuntimeException("Mesh file with provided path '" + meshPath + "' is empty.");
    }
    ServiceMesh serviceMesh = new ServiceMesh();
    try {
      ServiceMeshSpec spec = objectMapper.readValue(meshPathFile,
          ServiceMeshSpec.class);
      ServiceMeshDeploymentConfig serviceMeshDeploymentConfig = readDeploymentConfig(meshPath);
      spec.setDeploymentConfig(serviceMeshDeploymentConfig);
      serviceMesh.setSpec(spec);
      serviceMesh.getMetadata().setName(SERVICE_MESH_NAME);
    } catch (IOException e) {
      throw new RuntimeException(
          ExceptionUtils.appendLogSuggestion(
              "Unable to read mesh definition from '" + meshPath + "'.\n"
                  + "\n"
                  + "Details:\n"
                  + e.getMessage()), e);
    }
    return serviceMesh;
  }

  @NotNull
  public ConfigSourcesPaths extractConfigSourcesPaths(ServiceMesh serviceMesh) {
    Set<String> configEnvPaths = new HashSet<>();
    Set<String> secretEnvPaths = new HashSet<>();
    Set<String> configVolumePaths = new HashSet<>();
    Set<String> secretVolumePaths = new HashSet<>();
    processGlobalEnvSources(serviceMesh, configEnvPaths, secretEnvPaths);
    List<AbstractContainer> containers = extractContainers(serviceMesh);
    containers.forEach(container -> {
      EnvironmentFrom environmentFrom = container.getEnvironmentFrom();
      configEnvPaths.addAll(
          extractConfigSourcesPaths(environmentFrom, AbstractFromSource::getConfigs, null));
      secretEnvPaths.addAll(
          extractConfigSourcesPaths(environmentFrom, AbstractFromSource::getSecrets, null));
      VolumesFrom volumesFrom = container.getVolumesFrom();
      configVolumePaths.addAll(
          extractConfigSourcesPaths(volumesFrom, AbstractFromSource::getConfigs,
              this::mapToHostPath));
      secretVolumePaths.addAll(
          extractConfigSourcesPaths(volumesFrom, AbstractFromSource::getSecrets,
              this::mapToHostPath));
    });

    return new ConfigSourcesPaths(configEnvPaths, secretEnvPaths, configVolumePaths,
        secretVolumePaths);
  }

  @NotNull
  private List<String> extractConfigSourcesPaths(AbstractFromSource fromSource,
      Function<AbstractFromSource, List<String>> pathsExtractor, Function<String, String> mapper) {
    List<String> configsPaths = Collections.emptyList();
    if (fromSource != null) {
      List<String> extractedPaths = pathsExtractor.apply(fromSource);
      if (extractedPaths != null) {
        configsPaths = extractedPaths.stream().filter(Objects::nonNull).toList();
        if (mapper != null) {
          configsPaths = configsPaths.stream().map(mapper).collect(Collectors.toList());
        }
      }
    }
    return configsPaths;
  }

  @Nullable
  private ServiceMeshDeploymentConfig readDeploymentConfig(Path meshPath) {
    Path deploymentPath = projectPathsResolver.resolveDeploymentPath(meshPath);
    ServiceMeshDeploymentConfig serviceMeshDeploymentConfig = null;
    File deploymentFile = deploymentPath.toFile();
    if (deploymentFile.exists() && deploymentFile.length() > 0) {
      try {
        serviceMeshDeploymentConfig = objectMapper.readValue(deploymentFile,
            ServiceMeshDeploymentConfig.class);
      } catch (IOException e) {
        throw new RuntimeException(
            ExceptionUtils.appendLogSuggestion(
                "Unable to read deployment from '" + deploymentPath + "'.\n"
                    + "\n"
                    + "Details:\n"
                    + e.getMessage()), e);
      }
    }
    return serviceMeshDeploymentConfig;
  }

  @NotNull
  List<AbstractContainer> extractContainers(ServiceMesh serviceMesh) {
    ServiceMeshSpec serviceMeshSpec = serviceMesh.getSpec();
    List<AbstractContainer> containers = Stream.of(
            serviceMeshSpec.getIngestion(),
            serviceMeshSpec.getProcessing(),
            serviceMeshSpec.getDelivery()
        ).filter(Objects::nonNull).map(Map::values)
        .flatMap(Collection::stream).collect(Collectors.toList());
    containers.addAll(
        Optional.ofNullable(serviceMeshSpec.getDelivery())
            .orElse(Collections.emptyMap())
            .values()
            .stream()
            .map(DeliveryService::getComponents)
            .filter(Objects::nonNull)
            .map(Map::values)
            .flatMap(Collection::stream)
            .toList()
    );
    return containers;
  }

  private void processGlobalEnvSources(ServiceMesh serviceMesh, Set<String> envConfigsPaths,
      Set<String> envSecretsPaths) {
    EnvironmentFrom globalEnvironmentFrom = serviceMesh.getSpec().getEnvironmentFrom();
    if (globalEnvironmentFrom != null) {
      List<String> globalEnvironmentFromConfigs = globalEnvironmentFrom.getConfigs();
      if (globalEnvironmentFromConfigs != null) {
        envConfigsPaths.addAll(globalEnvironmentFromConfigs);
      }
      List<String> globalEnvironmentFromSecrets = globalEnvironmentFrom.getSecrets();
      if (globalEnvironmentFromSecrets != null) {
        envSecretsPaths.addAll(globalEnvironmentFromSecrets);
      }
    }
  }

  private String mapToHostPath(String volumeConf) {
    return volumeConf.split(":")[0];
  }

  public record ConfigSourcesPaths(Set<String> configEnvPaths, Set<String> secretEnvPaths,
                                   Set<String> configVolumePaths, Set<String> secretVolumePaths) {

  }
}
