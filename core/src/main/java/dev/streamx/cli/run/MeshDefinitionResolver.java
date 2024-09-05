package dev.streamx.cli.run;

import dev.streamx.cli.run.RunCommand.MeshSource;
import dev.streamx.runner.mapper.MeshConfigMapper;
import dev.streamx.runner.model.ServiceMesh;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.IOException;
import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;
import picocli.CommandLine.ParameterException;

@ApplicationScoped
class MeshDefinitionResolver {

  private static final String CURRENT_DIRECTORY_MESH = "./streamx-mesh.yml";

  private final MeshConfigMapper mapper = new MeshConfigMapper();

  @Inject
  CommandLine.ParseResult parseResult;

  @NotNull
  MeshDefinitionResolver.MeshDefinition resolve(MeshSource meshSource) throws IOException {
    if (meshSource == null) {
      return resolveCurrentDirectoryMeshDefinition();
    } else if (meshSource.meshDefinitionFile != null) {
      return resolveExplicitlyGivenMeshDefinitionFile(meshSource);
    } else {
      throw new ParameterException(parseResult.subcommand().commandSpec().commandLine(),
          "Mesh definition not found");
    }
  }

  @NotNull
  private MeshDefinitionResolver.MeshDefinition resolveCurrentDirectoryMeshDefinition()
      throws IOException {
    Path path;
    path = Path.of(CURRENT_DIRECTORY_MESH);
    if (path.toFile().exists()) {
      ServiceMesh serviceMesh = this.mapper.read(path);
      return new MeshDefinition(path, serviceMesh);
    } else {
      throw new ParameterException(parseResult.subcommand().commandSpec().commandLine(),
          "Missing mesh definition. Use '-f' to select mesh file or "
          + "make sure 'streamx-mesh.yml' exists in current directory.");
    }
  }

  @NotNull
  private MeshDefinitionResolver.MeshDefinition resolveExplicitlyGivenMeshDefinitionFile(
      MeshSource meshSource) throws IOException {
    Path path = Path.of(meshSource.meshDefinitionFile);
    ServiceMesh serviceMesh = this.mapper.read(path);

    return new MeshDefinition(path, serviceMesh);
  }

  record MeshDefinition(Path path, ServiceMesh serviceMesh) {

  }
}
