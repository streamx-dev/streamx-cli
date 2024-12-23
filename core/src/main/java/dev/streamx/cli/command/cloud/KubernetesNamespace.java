package dev.streamx.cli.command.cloud;

import java.util.Optional;
import picocli.CommandLine.Option;

public class KubernetesNamespace {

  public static final String DEFAULT_K8S_NAMESPACE = "default";

  @Option(names = {"-n", "--namespace"}, paramLabel = "<kubernetesNamespace>",
      description = "Deployment target Kubernetes namespace.",
      defaultValue = DEFAULT_K8S_NAMESPACE)
  public String namespace;

  public String getNamespace() {
    return namespace;
  }

  public static String getNamespace(KubernetesNamespace kubernetesNamespace) {
    return Optional.ofNullable(kubernetesNamespace).map(n -> n.getNamespace())
        .orElse(DEFAULT_K8S_NAMESPACE);
  }
}
