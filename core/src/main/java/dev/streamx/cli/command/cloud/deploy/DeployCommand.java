package dev.streamx.cli.command.cloud.deploy;

import static dev.streamx.cli.util.Output.printf;

import dev.streamx.cli.VersionProvider;
import dev.streamx.cli.command.cloud.KubernetesArguments;
import dev.streamx.cli.command.cloud.KubernetesService;
import dev.streamx.cli.command.cloud.ServiceMeshService;
import dev.streamx.cli.command.cloud.ServiceMeshService.ConfigSourcesPaths;
import dev.streamx.cli.command.meshprocessing.MeshResolver;
import dev.streamx.cli.command.meshprocessing.MeshSource;
import dev.streamx.operator.crd.ServiceMesh;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.Secret;
import jakarta.inject.Inject;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;

@Command(
    name = DeployCommand.COMMAND_NAME,
    mixinStandardHelpOptions = true,
    versionProvider = VersionProvider.class,
    description = "Deploy the StreamX project to the cloud. "
        + DeployCommand.CLOUD_COMMAND_DESCRIPTION
)
public class DeployCommand implements Runnable {

  public static final String COMMAND_NAME = "deploy";

  public static final String CLOUD_COMMAND_DESCRIPTION = """
      The command automatically uses the cluster connection and namespace settings from the \
      current context in your kubeconfig file. Ensure that your kubeconfig is configured correctly \
      and pointing to the desired cluster and namespace. You can verify your current context \
      and namespace by running:
              
        kubectl config current-context
        kubectl config view --minify | grep namespace
              
      If necessary, switch to the correct context using:
              
        kubectl config use-context <context-name>
              
      This command assumes the StreamX Operator is installed and the required CRDs are available \
      on the target cluster. If not, please install the operator and ensure the cluster meets \
      the prerequisites before running this command.
      """;

  @ArgGroup
  MeshSource meshSource;

  @ArgGroup
  KubernetesArguments namespaceArg;

  @Inject
  MeshResolver meshResolver;

  @Inject
  ServiceMeshService serviceMeshService;

  @Inject
  KubernetesService kubernetesService;

  @Inject
  ConfigService configService;

  @Override
  public void run() {
    Path meshPath = meshResolver.resolveMeshPath(meshSource);
    meshPath = meshPath.toAbsolutePath();
    ServiceMesh serviceMesh = serviceMeshService.getServiceMesh(meshPath);
    Path projectPath = meshPath.getParent();
    deploy(serviceMesh, projectPath);
  }

  private void deploy(ServiceMesh serviceMesh, Path projectPath) {
    kubernetesService.validateCrdInstallation();
    ConfigSourcesPaths fromSourcesPaths = serviceMeshService.getConfigSourcesPaths(serviceMesh);
    String serviceMeshName = serviceMesh.getMetadata().getName();
    deployConfigMaps(projectPath, fromSourcesPaths, serviceMeshName);
    deploySecrets(projectPath, fromSourcesPaths, serviceMeshName);
    kubernetesService.deploy(serviceMesh);
    printf("Project %s successfully deployed to '%s' namespace.",
        projectPath.toAbsolutePath().normalize(), kubernetesService.getNamespace());
  }

  private void deploySecrets(Path projectPath, ConfigSourcesPaths fromSourcesPaths,
      String serviceMeshName) {
    List<@NotNull Secret> envSecrets = getEnvSecrets(projectPath, fromSourcesPaths.secretEnvPaths(),
        serviceMeshName);
    List<@NotNull Secret> volumeSecrets = getVolumeSecrets(projectPath,
        fromSourcesPaths.secretVolumePaths(), serviceMeshName);
    List<Secret> secrets = Stream.concat(envSecrets.stream(), volumeSecrets.stream()).toList();
    kubernetesService.deploy(secrets);
  }

  private void deployConfigMaps(Path projectPath, ConfigSourcesPaths fromSourcesPaths,
      String serviceMeshName) {
    List<@NotNull ConfigMap> envConfigMaps = getEnvConfigMaps(projectPath,
        fromSourcesPaths.configEnvPaths(), serviceMeshName);
    List<@NotNull ConfigMap> volumeConfigMaps = getVolumeConfigMaps(projectPath,
        fromSourcesPaths.configVolumePaths(), serviceMeshName);
    List<ConfigMap> configMaps = Stream.concat(envConfigMaps.stream(), volumeConfigMaps.stream())
        .toList();
    kubernetesService.deploy(configMaps);
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
