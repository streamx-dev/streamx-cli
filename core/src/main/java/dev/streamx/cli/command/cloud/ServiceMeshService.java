package dev.streamx.cli.command.cloud;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
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
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

@ApplicationScoped
public class ServiceMeshService {

  public static final String SERVICE_MESH_NAME = "sx";
  protected static final String DEPLOYMENT_FILE_NAME = "deployment.yaml";
  private final ObjectMapper objectMapper = (new ObjectMapper(
      new YAMLFactory())).setSerializationInclusion(Include.NON_NULL);

  public record ConfigSourcesPaths(List<String> envConfigsPaths, List<String> envSecretsPaths,
                                   List<String> volumesConfigsPaths,
                                   List<String> volumesSecretsPaths) {

  }

  @NotNull
  public ServiceMesh getServiceMesh(Path meshPath) {
    Path deploymentPath = resolveDeploymentPath(meshPath);
    ServiceMesh serviceMesh = new ServiceMesh();
    try {
      ServiceMeshSpec spec = objectMapper.readValue(meshPath.toFile(),
          ServiceMeshSpec.class);
      if (deploymentPath.toFile().exists()) {
        ServiceMeshDeploymentConfig serviceMeshDeploymentConfig = objectMapper.readValue(
            deploymentPath.toFile(), ServiceMeshDeploymentConfig.class);
        spec.setDeploymentConfig(serviceMeshDeploymentConfig);
      }
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
  public List<AbstractContainer> getContainers(ServiceMesh serviceMesh) {
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

  @NotNull
  public ConfigSourcesPaths getConfigSourcesPaths(ServiceMesh serviceMesh) {
    List<AbstractContainer> containers = getContainers(serviceMesh);
    List<String> envConfigsPaths = new ArrayList<>();
    List<String> envSecretsPaths = new ArrayList<>();
    List<String> volumesConfigsPaths = new ArrayList<>();
    List<String> volumesSecretsPaths = new ArrayList<>();
    containers.forEach(container -> {
      EnvironmentFrom environmentFrom = container.getEnvironmentFrom();
      envConfigsPaths.addAll(
          getConfigSourcesPaths(environmentFrom, AbstractFromSource::getConfigs, null));
      envSecretsPaths.addAll(
          getConfigSourcesPaths(environmentFrom, AbstractFromSource::getSecrets, null));
      VolumesFrom volumesFrom = container.getVolumesFrom();
      volumesConfigsPaths.addAll(getConfigSourcesPaths(volumesFrom, AbstractFromSource::getConfigs,
          ServiceMeshService::mapToHostPath));
      volumesSecretsPaths.addAll(getConfigSourcesPaths(volumesFrom, AbstractFromSource::getSecrets,
          ServiceMeshService::mapToHostPath));
    });
    return new ConfigSourcesPaths(envConfigsPaths, envSecretsPaths, volumesConfigsPaths,
        volumesSecretsPaths);
  }

  @NotNull
  private static List<String> getConfigSourcesPaths(AbstractFromSource fromSource,
      Function<AbstractFromSource, List<String>> pathsExtractor, Function<String, String> mapper) {
    List<String> configsPaths = Collections.emptyList();
    if (fromSource != null) {
      List<String> extractedPaths = pathsExtractor.apply(fromSource);
      if (extractedPaths != null) {
        configsPaths = extractedPaths;
        if (mapper != null) {
          configsPaths = configsPaths.stream().map(mapper).collect(Collectors.toList());
        }
      }
    }
    return configsPaths;
  }

  private static String mapToHostPath(String volumeConf) {
    return volumeConf.split(":")[0];
  }

  @NotNull
  static Path resolveDeploymentPath(Path meshPath) {
    String meshFileName = meshPath.getFileName().toString();
    meshFileName = StringUtils.removeEnd(meshFileName, ".yaml");
    meshFileName = StringUtils.removeEnd(meshFileName, ".yml");
    String deploymentFileName = DEPLOYMENT_FILE_NAME;
    if (!"mesh".equals(meshFileName)) {
      deploymentFileName = meshFileName + "." + deploymentFileName;
    }
    return meshPath.getParent().resolve(deploymentFileName);
  }
}
