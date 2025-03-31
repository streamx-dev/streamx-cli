package dev.streamx.cli.command.cloud;

import dev.streamx.cli.config.ArgumentConfigSource;
import picocli.CommandLine.Option;

public class KubernetesArguments {

  @Option(names = {"-n", "--namespace"}, paramLabel = "<kubernetesNamespace>",
      description = "Forces provided namespace.")
  void namespace(String namespace) {
    ArgumentConfigSource.registerValue(KubernetesConfig.STREAMX_KUBERNETES_NAMESPACE, namespace);
  }

  @Option(names = {"-d", "--resources-directories"}, paramLabel = "<resourceDirectories>",
      description = "Specifies one or more comma-separated relative directory paths (from the "
          + "mesh.yaml directory) where managed Kubernetes resource definitions are located.")
  void resourceDirectories(String resourceDirectories) {
    ArgumentConfigSource.registerValue(KubernetesConfig.STREAMX_KUBERNETES_RESOURCE_DIRECTORIES,
        resourceDirectories);
  }

  @Option(names = {"-r",
      "--controlled-resource-definitions"}, paramLabel = "<controlledResourceDefinitions>",
      description = "Specifies one or more comma-separated definitions in the form of"
          + " [cluster:]group/version/kind to be managed by CLI during undeploy.")
  void controlledResourceDefinitions(String resourceDefinitions) {
    ArgumentConfigSource.registerValue(
        KubernetesConfig.STREAMX_KUBERNETES_CONTROLLED_RESOURCE_DEFINITIONS, resourceDefinitions);
  }
}
