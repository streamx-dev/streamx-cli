package dev.streamx.cli.command.cloud.deploy;

import static dev.streamx.cli.util.Output.printf;

import dev.streamx.cli.VersionProvider;
import dev.streamx.cli.command.cloud.KubernetesNamespace;
import dev.streamx.cli.command.cloud.ServiceMeshService;
import dev.streamx.cli.command.cloud.ServiceMeshService.ConfigSourcesPaths;
import dev.streamx.cli.command.cloud.deploy.DataService.ConfigType;
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

  @Inject
  DataService dataService;

  @Override
  public void run() {
    Path meshPath = meshDefinitionResolver.resolveMeshPath(meshSource);
    meshPath = meshPath.toAbsolutePath();
    ServiceMesh serviceMesh = serviceMeshService.getServiceMesh(meshPath);
    Path projectPath = meshPath.getParent();
    deploy(serviceMesh, projectPath);
  }

  private void deploy(ServiceMesh serviceMesh, Path projectPath) {
    ConfigSourcesPaths fromSourcesPaths = serviceMeshService.getConfigSourcesPaths(serviceMesh);
    List<ConfigMap> configMaps = new ArrayList<>();
    List<Secret> secrets = new ArrayList<>();
    String serviceMeshName = serviceMesh.getMetadata().getName();

    fromSourcesPaths.envConfigsPaths().forEach(path -> {
      Path resolvedPath = dataService.resolveConfigPath(projectPath, path);
      Map<String, String> data = dataService.loadDataMapFromEnvFile(resolvedPath);
      ConfigMap configMap = kubernetesService.buildConfigMap(serviceMeshName, path, data,
          ConfigType.FILE);
      configMaps.add(configMap);
    });

    fromSourcesPaths.envSecretsPaths().forEach(path -> {
      Path resolvedPath = dataService.resolveSecretPath(projectPath, path);
      Map<String, String> data = dataService.loadDataMapFromEnvFile(resolvedPath);
      Secret secret = kubernetesService.buildSecret(serviceMeshName, path, data, ConfigType.FILE);
      secrets.add(secret);
    });

    fromSourcesPaths.volumesConfigsPaths().forEach(path -> {
      Path resolvedPath = dataService.resolveConfigPath(projectPath, path);
      Map<String, String> data = dataService.loadDataMapFromPath(resolvedPath);
      ConfigType configType = dataService.getConfigType(resolvedPath);
      ConfigMap configMap = kubernetesService.buildConfigMap(serviceMeshName, path, data,
          configType);
      configMaps.add(configMap);
    });

    fromSourcesPaths.volumesSecretsPaths().forEach(path -> {
      Path resolvedPath = dataService.resolveSecretPath(projectPath, path);
      Map<String, String> data = dataService.loadDataMapFromPath(resolvedPath);
      ConfigType configType = dataService.getConfigType(resolvedPath);
      Secret secret = kubernetesService.buildSecret(serviceMeshName, path, data, configType);
      secrets.add(secret);
    });

    String namespace = KubernetesNamespace.getNamespace(namespaceArg);
    kubernetesService.deploy(configMaps, namespace);
    kubernetesService.deploy(secrets, namespace);
    kubernetesService.deploy(serviceMesh, namespace);

    printf("%s successfully deployed to '%s' namespace.",
        projectPath.toAbsolutePath().normalize(),
        namespace);
  }
}
