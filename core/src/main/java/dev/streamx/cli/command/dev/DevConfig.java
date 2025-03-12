package dev.streamx.cli.command.dev;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

@ConfigMapping
public interface DevConfig {

  String STREAMX_DEV_DASHBOARD_PORT = "streamx.dev.dashboard.port";
  String STREAMX_DEV_DASHBOARD_OPEN_ON_STARTUP = "streamx.dev.dashboard.open-on-startup";

  @WithName(STREAMX_DEV_DASHBOARD_PORT)
  @WithDefault("9088")
  int dashboardPort();

  @WithName(STREAMX_DEV_DASHBOARD_OPEN_ON_STARTUP)
  @WithDefault("true")
  boolean openOnStart();
}
