package dev.streamx.cli.command.dev;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

@ConfigMapping
public interface DevConfig {

  String STREAMX_DEV_DASHBOARD_PORT = "streamx.dev.dashboard.port";

  @WithName(STREAMX_DEV_DASHBOARD_PORT)
  @WithDefault("9088")
  int dashboardPort();
}
