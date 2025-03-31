package dev.streamx.cli.command.meshprocessing;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithName;
import java.util.Optional;

@ConfigMapping
public interface MeshConfig {

  String STREAMX_MESH_PATH = "streamx.mesh-path";

  @WithName(STREAMX_MESH_PATH)
  Optional<String> meshDefinitionFile();
}
