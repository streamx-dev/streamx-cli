package dev.streamx.cli.command.cloud.deploy;

import static dev.streamx.cli.util.Output.printf;

import dev.streamx.cli.VersionProvider;
import dev.streamx.cli.command.cloud.KubernetesNamespace;
import dev.streamx.cli.command.cloud.ServiceMeshService;
import dev.streamx.cli.command.cloud.ServiceMeshService.ConfigSourcesPaths;
import dev.streamx.cli.command.run.MeshDefinitionResolver;
import dev.streamx.cli.command.run.RunCommand.MeshSource;
import dev.streamx.operator.crd.ServiceMesh;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.Secret;
import jakarta.inject.Inject;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
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
  ConfigService configService;

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

    fromSourcesPaths.configEnvPaths().stream()
        .map(path -> configService.getConfigEnv(projectPath, path))
        .map(config -> kubernetesService.buildConfigMap(serviceMeshName, config))
        .forEach(configMaps::add);

    fromSourcesPaths.secretEnvPaths().stream()
        .map(path -> configService.getSecretEnv(projectPath, path))
        .map(config -> kubernetesService.buildSecret(serviceMeshName, config))
        .forEach(secrets::add);

    fromSourcesPaths.configVolumePaths().stream()
        .map(path -> configService.getConfigVolume(projectPath, path))
        .map(config -> kubernetesService.buildConfigMap(serviceMeshName, config))
        .forEach(configMaps::add);

    fromSourcesPaths.secretVolumePaths().stream()
        .map(path -> configService.getSecretVolume(projectPath, path))
        .map(config -> kubernetesService.buildSecret(serviceMeshName, config))
        .forEach(secrets::add);

    String namespace = KubernetesNamespace.getNamespace(namespaceArg);
    kubernetesService.deploy(configMaps, namespace);
    kubernetesService.deploy(secrets, namespace);
    kubernetesService.deploy(serviceMesh, namespace);

    printf("%s successfully deployed to '%s' namespace.", projectPath.toAbsolutePath().normalize(),
        namespace);
  }
}
