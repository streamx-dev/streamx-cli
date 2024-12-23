package dev.streamx.cli.command.cloud.deploy;

import static dev.streamx.cli.util.Output.printf;

import dev.streamx.cli.VersionProvider;
import dev.streamx.cli.command.cloud.KubernetesNamespace;
import dev.streamx.cli.command.cloud.ServiceMeshService;
import dev.streamx.cli.command.cloud.ServiceMeshService.FromSourcePaths;
import dev.streamx.cli.command.run.MeshDefinitionResolver;
import dev.streamx.cli.command.run.RunCommand.MeshSource;
import dev.streamx.operator.crd.ServiceMesh;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.Secret;
import jakarta.inject.Inject;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;

@Command(
    name = DeployCommand.COMMAND_NAME,
    mixinStandardHelpOptions = true,
    versionProvider = VersionProvider.class,
    description = "Deploy StreamX project to the cloud."
)
public class DeployCommand implements Runnable {

  public static final String COMMAND_NAME = "deploy";
  public static final String CONFIGS_DIRECTORY = "configs";
  public static final String SECRETS_DIRECTORY = "secrets";

  @ArgGroup
  MeshSource meshSource;

  @ArgGroup
  KubernetesNamespace namespaceArg;

  @Inject
  MeshDefinitionResolver meshDefinitionResolver;

  @Inject
  ServiceMeshService serviceMeshService;

  @Inject
  KubernetesService kubernetesService;

  @Override
  public void run() {
    Path meshPath = meshDefinitionResolver.resolveMeshPath(meshSource);
    ServiceMesh serviceMesh = serviceMeshService.getServiceMesh(meshPath);
    Path projectPath = meshPath.getParent();
    deploy(serviceMesh, projectPath);
  }

  private void deploy(ServiceMesh serviceMesh, Path projectPath) {
    FromSourcePaths fromSourcesPaths = serviceMeshService.getFromSourcesPaths(serviceMesh);
    List<ConfigMap> configMaps = new ArrayList<>();
    List<Secret> secrets = new ArrayList<>();
    String serviceMeshName = serviceMesh.getMetadata().getName();
    fromSourcesPaths.envConfigsPaths().stream()
        .map(mapFromSourcePathToConfigMap(serviceMeshName, projectPath,
            DataResolver::loadDataMapFromEnvFile))
        .forEach(configMaps::add);
    fromSourcesPaths.envSecretsPaths().stream()
        .map(mapFromSourcePathToSecret(serviceMeshName, projectPath,
            DataResolver::loadDataMapFromEnvFile))
        .forEach(secrets::add);
    fromSourcesPaths.volumesConfigsPaths().stream()
        .map(mapFromSourcePathToConfigMap(serviceMeshName, projectPath,
            DataResolver::loadDataMapFromPath))
        .forEach(configMaps::add);
    fromSourcesPaths.volumesSecretsPaths().stream()
        .map(mapFromSourcePathToSecret(serviceMeshName, projectPath,
            DataResolver::loadDataMapFromPath))
        .forEach(secrets::add);
    String namespace = KubernetesNamespace.getNamespace(namespaceArg);
    kubernetesService.deploy(configMaps, namespace);
    kubernetesService.deploy(secrets, namespace);
    kubernetesService.deploy(serviceMesh, namespace);
    printf("%s successfully deployed to '%s' namespace.",
        projectPath.toAbsolutePath().normalize(),
        namespace);
  }

  @NotNull
  private Function<String, Secret> mapFromSourcePathToSecret(String serviceMeshName,
      Path projectPath, Function<Path, Map<String, String>> pathToDataMapper) {
    return path -> {
      Path dataPath = projectPath.resolve(SECRETS_DIRECTORY).resolve(path);
      Map<String, String> dataMap = pathToDataMapper.apply(dataPath);
      return kubernetesService.buildSecret(serviceMeshName, path, dataMap);
    };
  }

  @NotNull
  private Function<String, ConfigMap> mapFromSourcePathToConfigMap(String serviceMeshName,
      Path projectPath, Function<Path, Map<String, String>> pathToDataMapper) {
    return path -> {
      Path dataPath = projectPath.resolve(CONFIGS_DIRECTORY).resolve(path);
      Map<String, String> dataMap = pathToDataMapper.apply(dataPath);
      return kubernetesService.buildConfigMap(serviceMeshName, path, dataMap);
    };
  }
}
