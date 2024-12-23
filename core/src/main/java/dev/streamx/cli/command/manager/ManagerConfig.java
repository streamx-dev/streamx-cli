package dev.streamx.cli.command.manager;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

@ConfigMapping
public interface ManagerConfig {

  String STREAMX_MANAGER_MESH_MANAGER_PORT = "streamx.manager.mesh-manager.port";

  @WithName(STREAMX_MANAGER_MESH_MANAGER_PORT)
  @WithDefault("9088")
  int meshManagerPort();
}
