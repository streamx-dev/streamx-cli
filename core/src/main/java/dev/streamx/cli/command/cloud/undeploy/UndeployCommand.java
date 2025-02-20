package dev.streamx.cli.command.cloud.undeploy;

import static dev.streamx.cli.command.cloud.ServiceMeshResolver.SERVICE_MESH_NAME;
import static dev.streamx.cli.util.Output.printf;

import dev.streamx.cli.VersionProvider;
import dev.streamx.cli.command.cloud.KubernetesArguments;
import dev.streamx.cli.command.cloud.KubernetesService;
import dev.streamx.cli.command.cloud.ServiceMeshResolver;
import dev.streamx.cli.command.cloud.deploy.DeployCommand;
import dev.streamx.cli.command.cloud.deploy.ProjectResourcesExtractor;
import dev.streamx.cli.command.meshprocessing.MeshResolver;
import dev.streamx.cli.command.meshprocessing.MeshSource;
import dev.streamx.operator.crd.ServiceMesh;
import jakarta.inject.Inject;
import java.nio.file.Path;
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

  @ArgGroup(exclusive = false)
  KubernetesArguments kubernetesArguments;
  @Inject
  KubernetesService kubernetesService;

  @Override
  public void run() {
    kubernetesService.validateCrdInstallation();
    kubernetesService.undeploy(SERVICE_MESH_NAME);

    printf("StreamX project successfully undeployed from '%s' namespace.",
        kubernetesService.getNamespace());
  }
}
