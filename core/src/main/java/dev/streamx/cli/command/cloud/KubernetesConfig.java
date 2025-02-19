package dev.streamx.cli.command.cloud;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithName;
import java.util.Optional;

@ConfigMapping
public interface KubernetesConfig {

  String STREAMX_KUBERNETES_NAMESPACE = "streamx.kubernetes.namespace";
  String STREAMX_KUBERNETES_RESOURCES_DIRECTORIES = "streamx.kubernetes.resources-directories";

  @WithName(STREAMX_KUBERNETES_NAMESPACE)
  Optional<String> namespace();

  @WithName(STREAMX_KUBERNETES_RESOURCES_DIRECTORIES)
  Optional<String> resourcesDirectories();
}
