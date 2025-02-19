package dev.streamx.cli.command.cloud;

import dev.streamx.cli.command.cloud.KubernetesConfig;
import dev.streamx.cli.config.ArgumentConfigSource;
import picocli.CommandLine.Option;

public class KubernetesResourcesArguments {

  @Option(names = {"-d", "--resources-directories"}, paramLabel = "<resourcesDirectories>",
      description = "Specifies one or more comma-separated relative directory paths (from the "
          + "mesh.yaml directory) where managed Kubernetes resource definitions are located.")
  void resourcesDirectories(String resourcesDirectories) {
    ArgumentConfigSource.registerValue(KubernetesConfig.STREAMX_KUBERNETES_RESOURCES_DIRECTORIES,
        resourcesDirectories);
  }

}
