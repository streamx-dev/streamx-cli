package dev.streamx.cli.command.cloud.delete;

import static dev.streamx.cli.command.cloud.ServiceMeshService.SERVICE_MESH_NAME;
import static dev.streamx.cli.util.Output.printf;

import dev.streamx.cli.VersionProvider;
import dev.streamx.cli.command.cloud.KubernetesNamespace;
import dev.streamx.operator.crd.ServiceMesh;
import io.fabric8.kubernetes.client.KubernetesClient;
import jakarta.inject.Inject;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;

@Command(
    name = DeleteCommand.COMMAND_NAME,
    mixinStandardHelpOptions = true,
    versionProvider = VersionProvider.class,
    description = "Delete StreamX Service Mesh from cloud."
)
public class DeleteCommand implements Runnable {

  public static final String COMMAND_NAME = "delete";

  @ArgGroup
  KubernetesNamespace namespaceArg;

  @Inject
  KubernetesClient kubernetesClient;

  @Override
  public void run() {
    String namespace = KubernetesNamespace.getNamespace(namespaceArg);
    kubernetesClient.resources(ServiceMesh.class).inNamespace(namespace).withName(SERVICE_MESH_NAME)
        .delete();
    printf("ServiceMesh '%s' successfully deleted from '%s' namespace.", SERVICE_MESH_NAME,
        namespace);
  }
}
