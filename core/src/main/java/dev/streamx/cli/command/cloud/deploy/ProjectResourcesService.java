package dev.streamx.cli.command.cloud.deploy;

import dev.streamx.cli.command.cloud.KubernetesService;
import dev.streamx.cli.command.cloud.ServiceMeshService.ConfigSourcesPaths;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.Secret;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;

@ApplicationScoped
public class ProjectResourcesService {

  @Inject
  ConfigService configService;

  @Inject
  KubernetesService kubernetesService;

  @NotNull
  public List<Secret> getSecrets(Path projectPath, ConfigSourcesPaths configSourcesPaths,
      String serviceMeshName) {
    List<@NotNull Secret> envSecrets = getEnvSecrets(projectPath,
        configSourcesPaths.secretEnvPaths(),
        serviceMeshName);
    List<@NotNull Secret> volumeSecrets = getVolumeSecrets(projectPath,
        configSourcesPaths.secretVolumePaths(), serviceMeshName);
    return Stream.concat(envSecrets.stream(), volumeSecrets.stream()).toList();
  }

  @NotNull
  public List<ConfigMap> getConfigMaps(Path projectPath, ConfigSourcesPaths configSourcesPaths,
      String serviceMeshName) {
    List<@NotNull ConfigMap> envConfigMaps = getEnvConfigMaps(projectPath,
        configSourcesPaths.configEnvPaths(), serviceMeshName);
    List<@NotNull ConfigMap> volumeConfigMaps = getVolumeConfigMaps(projectPath,
        configSourcesPaths.configVolumePaths(), serviceMeshName);
    return Stream.concat(envConfigMaps.stream(), volumeConfigMaps.stream()).toList();
  }

  @NotNull
  private List<@NotNull Secret> getVolumeSecrets(Path projectPath, Set<String> volumePaths,
      String serviceMeshName) {
    return volumePaths.stream()
        .map(path -> configService.getSecretVolume(projectPath, path))
        .map(config -> kubernetesService.buildSecret(serviceMeshName, config))
        .toList();
  }

  @NotNull
  private List<@NotNull Secret> getEnvSecrets(Path projectPath, Set<String> envPaths,
      String serviceMeshName) {
    return envPaths.stream()
        .map(path -> configService.getSecretEnv(projectPath, path))
        .map(config -> kubernetesService.buildSecret(serviceMeshName, config))
        .toList();
  }

  @NotNull
  private List<@NotNull ConfigMap> getEnvConfigMaps(Path projectPath, Set<String> envPaths,
      String serviceMeshName) {
    return envPaths.stream()
        .map(path -> configService.getConfigEnv(projectPath, path))
        .map(config -> kubernetesService.buildConfigMap(serviceMeshName, config))
        .toList();
  }

  @NotNull
  private List<@NotNull ConfigMap> getVolumeConfigMaps(Path projectPath, Set<String> volumePaths,
      String serviceMeshName) {
    return volumePaths.stream()
        .map(path -> configService.getConfigVolume(projectPath, path))
        .map(config -> kubernetesService.buildConfigMap(serviceMeshName, config))
        .toList();
  }
}
