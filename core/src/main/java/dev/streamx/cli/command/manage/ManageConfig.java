package dev.streamx.cli.command.manage;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

@ConfigMapping
public interface ManageConfig {

  String STREAMX_MANAGE_MESH_MANAGER_PORT = "streamx.manage.mesh-manager.port";
  String STREAMX_MANAGE_MESH_MANAGER_IMAGE = "streamx.manage.mesh-manager.image";

  @WithName(STREAMX_MANAGE_MESH_MANAGER_PORT)
  @WithDefault("9088")
  int meshManagerPort();

  @WithName(STREAMX_MANAGE_MESH_MANAGER_IMAGE)
  String meshManagerImage();

}
