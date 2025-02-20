package dev.streamx.cli.command.cloud;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithName;
import java.util.Optional;

@ConfigMapping
public interface KubernetesConfig {

  String STREAMX_KUBERNETES_NAMESPACE = "streamx.kubernetes.namespace";
  String STREAMX_KUBERNETES_RESOURCE_DIRECTORIES = "streamx.kubernetes.resource-directories";
  String STREAMX_KUBERNETES_CONTROLLED_RESOURCE_DEFINITIONS
      = "streamx.kubernetes.controlled-resource-definitions";

  @WithName(STREAMX_KUBERNETES_NAMESPACE)
  Optional<String> namespace();

  @WithName(STREAMX_KUBERNETES_RESOURCE_DIRECTORIES)
  Optional<String> resourceDirectories();

  @WithName(STREAMX_KUBERNETES_CONTROLLED_RESOURCE_DEFINITIONS)
  Optional<String> controlledResourceDefinitions();

}
