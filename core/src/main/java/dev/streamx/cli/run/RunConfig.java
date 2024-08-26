package dev.streamx.cli.run;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithName;
import java.util.Optional;

@ConfigMapping
public interface RunConfig {

  String STREAMX_CONTAINER_STARTUP_TIMEOUT_SECONDS = "streamx.container.startup-timeout-seconds";

  String STREAMX_OBSERVABILITY_ENABLED = "streamx.observability.enabled";

  @WithName(STREAMX_CONTAINER_STARTUP_TIMEOUT_SECONDS)
  Optional<Long> containerStartupTimeoutSeconds();

  @WithName(STREAMX_OBSERVABILITY_ENABLED)
  Optional<Boolean> observabilityEnabled();
}
