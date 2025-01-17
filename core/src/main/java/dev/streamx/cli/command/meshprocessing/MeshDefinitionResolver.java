package dev.streamx.cli.command.meshprocessing;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.streamx.cli.interpolation.Interpolating;
import dev.streamx.mesh.model.ServiceMesh;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.IOException;
import java.nio.file.Path;

@ApplicationScoped
public class MeshDefinitionResolver {

  @Inject
  @Interpolating
  ObjectMapper objectMapper;

  public ServiceMesh resolve(Path meshPath) throws IOException {
    return objectMapper.readValue(meshPath.toFile(), ServiceMesh.class);
  }
}
