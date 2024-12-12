package dev.streamx.cli.command.run;

import dev.streamx.cli.path.CurrentDirectoryProvider;
import dev.streamx.mesh.mapper.MeshConfigMapper;
import dev.streamx.mesh.model.ServiceMesh;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.IOException;
import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

@ApplicationScoped
public class MeshDefinitionResolver {

  private final MeshConfigMapper mapper = new MeshConfigMapper();

  @Inject
  CommandLine.ParseResult parseResult;

  @Inject
  CurrentDirectoryProvider currentDirectoryProvider;

  @NotNull
  MeshDefinitionResolver.MeshDefinition resolve(Path meshPath) throws IOException {
    return resolveMesh(meshPath);
  }

  @NotNull
  private MeshDefinition resolveMesh(Path pathToYaml) throws IOException {
    ServiceMesh serviceMesh = this.mapper.read(pathToYaml);
    return new MeshDefinition(pathToYaml, serviceMesh);
  }

  record MeshDefinition(Path path, ServiceMesh serviceMesh) {

  }
}
