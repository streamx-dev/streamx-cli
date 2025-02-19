package dev.streamx.cli.command.cloud;

import dev.streamx.cli.config.ArgumentConfigSource;
import picocli.CommandLine.Option;

public class KubernetesNamespaceArguments {

  @Option(names = {"-n", "--namespace"}, paramLabel = "<kubernetesNamespace>",
      description = "Forces provided namespace.")
  void namespace(String namespace) {
    ArgumentConfigSource.registerValue(KubernetesConfig.STREAMX_KUBERNETES_NAMESPACE, namespace);
  }
}
