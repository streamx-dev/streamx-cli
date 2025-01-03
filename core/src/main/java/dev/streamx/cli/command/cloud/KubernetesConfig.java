package dev.streamx.cli.command.cloud;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithName;
import java.util.Optional;

@ConfigMapping
public interface KubernetesConfig {

  String STREAMX_K8S_NAMESPACE = "streamx.cloud.namespace";

  @WithName(STREAMX_K8S_NAMESPACE)
  Optional<String> namespace();
}
