package dev.streamx.cli.command.meshprocessing;

import dev.streamx.mesh.mapper.MeshConfigMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;

@Dependent
public class MeshProcessingConfig {

  @ApplicationScoped
  MeshConfigMapper meshConfigMapper() {
    return new MeshConfigMapper();
  }
}
