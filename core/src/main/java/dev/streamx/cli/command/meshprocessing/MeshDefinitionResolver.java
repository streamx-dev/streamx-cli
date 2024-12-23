package dev.streamx.cli.command.meshprocessing;

import dev.streamx.mesh.mapper.MeshConfigMapper;
import dev.streamx.mesh.model.ServiceMesh;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.IOException;
import java.nio.file.Path;

@ApplicationScoped
public class MeshDefinitionResolver {

  @Inject
  MeshConfigMapper meshConfigMapper;

  public ServiceMesh resolve(Path meshPath) throws IOException {
    return this.meshConfigMapper.read(meshPath);
  }
}
