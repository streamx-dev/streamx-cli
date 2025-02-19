package dev.streamx.cli.command.cloud.undeploy;

import static dev.streamx.cli.util.Output.printf;

import dev.streamx.cli.VersionProvider;
import dev.streamx.cli.command.cloud.KubernetesNamespaceArguments;
import dev.streamx.cli.command.cloud.KubernetesResourcesArguments;
import dev.streamx.cli.command.cloud.KubernetesService;
import dev.streamx.cli.command.cloud.ServiceMeshResolver;
import dev.streamx.cli.command.cloud.deploy.DeployCommand;
import dev.streamx.cli.command.cloud.deploy.ProjectResourcesExtractor;
import dev.streamx.cli.command.meshprocessing.MeshResolver;
import dev.streamx.cli.command.meshprocessing.MeshSource;
import dev.streamx.operator.crd.ServiceMesh;
import io.fabric8.kubernetes.api.model.HasMetadata;
import jakarta.inject.Inject;
import java.nio.file.Path;
import java.util.List;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;

@Command(
    name = UndeployCommand.COMMAND_NAME,
    mixinStandardHelpOptions = true,
    versionProvider = VersionProvider.class,
    description = "Undeploy the StreamX Project from the cloud.",
    footer = DeployCommand.CLOUD_COMMAND_FOOTER
)
public class UndeployCommand implements Runnable {

  public static final String COMMAND_NAME = "undeploy";

  @ArgGroup
  MeshSource meshSource;
  @ArgGroup
  KubernetesNamespaceArguments kubernetesNamespaceArguments;
  @ArgGroup
  KubernetesResourcesArguments kubernetesResourcesArguments;
  @Inject
  ProjectResourcesExtractor projectResourcesExtractor;
  @Inject
  KubernetesService kubernetesService;
  @Inject
  MeshResolver meshResolver;
  @Inject
  ServiceMeshResolver serviceMeshResolver;

  @Override
  public void run() {
    Path meshPath = meshResolver.resolveMeshPath(meshSource);
    meshPath = meshPath.toAbsolutePath();
    ServiceMesh serviceMesh = serviceMeshResolver.resolveMesh(meshPath);
    Path projectPath = meshPath.getParent();
    String serviceMeshName = serviceMesh.getMetadata().getName();

    kubernetesService.validateCrdInstallation();
    kubernetesService.undeploy(serviceMeshName);

    undeployKubernetesResources(projectPath, serviceMeshName);
    printf("StreamX project successfully undeployed from '%s' namespace.",
        kubernetesService.getNamespace());
  }

  private void undeployKubernetesResources(Path projectPath, String serviceMeshName) {
    List<String> resourcesDirectory = kubernetesService.getResourcesPaths();
    List<HasMetadata> resources = projectResourcesExtractor.getResources(resourcesDirectory,
        projectPath, serviceMeshName);

    kubernetesService.undeploy(resources);
  }

}
