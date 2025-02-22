package dev.streamx.cli.command.cloud.deploy;

import static dev.streamx.cli.util.Output.printf;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.streamx.cli.VersionProvider;
import dev.streamx.cli.command.cloud.KubernetesArguments;
import dev.streamx.cli.command.cloud.KubernetesService;
import dev.streamx.cli.command.cloud.ServiceMeshResolver;
import dev.streamx.cli.command.cloud.ServiceMeshResolver.ConfigSourcesPaths;
import dev.streamx.cli.command.cloud.collector.DirectoryResourcesCollector;
import dev.streamx.cli.command.cloud.collector.KubernetesResourcesCollector;
import dev.streamx.cli.command.meshprocessing.MeshResolver;
import dev.streamx.cli.command.meshprocessing.MeshSource;
import dev.streamx.cli.interpolation.Interpolating;
import dev.streamx.operator.crd.ServiceMesh;
import io.fabric8.kubernetes.api.model.HasMetadata;
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
    description = "Deploy the StreamX project to the cloud.",
    footer = DeployCommand.CLOUD_COMMAND_FOOTER
)
public class DeployCommand implements Runnable {

  public static final String COMMAND_NAME = "deploy";

  public static final String CLOUD_COMMAND_FOOTER = """
            
      The command automatically uses the cluster connection and namespace settings from the \
      current context in your @|italic kubeconfig|@ file. Ensure that your @|italic kubeconfig|@ \
      is configured correctly and pointing to the desired cluster and namespace. You can verify \
      your current context and namespace by running:
              
          @|yellow kubectl config current-context|@
          @|yellow kubectl config view --minify | grep namespace|@
              
      If necessary, switch to the correct context using:
              
          @|yellow kubectl config use-context <context-name>|@
              
      This command assumes the StreamX Operator is installed and the required CRDs are available \
      on the target cluster. If not, please install the operator and ensure the cluster meets \
      the prerequisites before running this command.""";

  @ArgGroup
  MeshSource meshSource;

  @ArgGroup(exclusive = false)
  KubernetesArguments kubernetesArguments;

  @Inject
  MeshResolver meshResolver;

  @Inject
  ServiceMeshResolver serviceMeshResolver;

  @Inject
  KubernetesService kubernetesService;

  @Inject
  ProjectResourcesExtractor projectResourcesExtractor;

  @Inject
  @Interpolating
  ObjectMapper objectMapper;

  @Override
  public void run() {
    Path meshPath = meshResolver.resolveMeshPath(meshSource);
    meshPath = meshPath.toAbsolutePath();
    ServiceMesh serviceMesh = serviceMeshResolver.resolveMesh(meshPath);
    Path projectPath = meshPath.getParent();
    deploy(serviceMesh, projectPath);
  }

  private void deploy(ServiceMesh serviceMesh, Path projectPath) {
    kubernetesService.validateCrdInstallation();
    ConfigSourcesPaths configPaths = serviceMeshResolver.extractConfigSourcesPaths(serviceMesh);
    String serviceMeshName = serviceMesh.getMetadata().getName();

    List<HasMetadata> resourcesToDeploy = new ArrayList<>();
    List<HasMetadata> managedResources = kubernetesService.collectManagedResources(serviceMeshName);

    // Collect all resources to deploy
    resourcesToDeploy.addAll(collectKubernetesResources(projectPath, serviceMeshName));
    resourcesToDeploy.addAll(
        projectResourcesExtractor.getSecrets(projectPath, configPaths, serviceMeshName));
    resourcesToDeploy.addAll(
        projectResourcesExtractor.getConfigMaps(projectPath, configPaths, serviceMeshName));
    resourcesToDeploy.add(serviceMesh);

    // Collect all resources to delete
    ResourceCleaner cleaner = new ResourceCleaner(resourcesToDeploy, managedResources);
    kubernetesService.deploy(resourcesToDeploy);
    printf("Project %s successfully deployed to '%s' namespace.\n",
        projectPath.toAbsolutePath().normalize(), kubernetesService.getNamespace());
    List<HasMetadata> orphanedResources = cleaner.getOrphanedResources();
    printf("Deleting %d orphaned resources.\n", orphanedResources.size());
    kubernetesService.undeploy(orphanedResources);

  }

  private List<HasMetadata> collectKubernetesResources(Path projectPath, String serviceMeshName) {
    List<String> resourcesDirectories = kubernetesService.getResourcePaths();

    KubernetesResourcesCollector collector = new DirectoryResourcesCollector(objectMapper,
        projectPath, resourcesDirectories);
    return collector.collect(serviceMeshName);
  }
}
