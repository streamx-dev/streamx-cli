package dev.streamx.cli.command.cloud;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithName;
import java.util.Optional;

@ConfigMapping
public interface KubernetesConfig {

  String STREAMX_CLOUD_NAMESPACE = "streamx.cloud.namespace";

  @WithName(STREAMX_CLOUD_NAMESPACE)
  Optional<String> namespace();
}
