package dev.streamx.cli.command.run;

import static dev.streamx.cli.util.Output.printf;

import dev.streamx.cli.command.run.RunCommand.MeshSource;
import dev.streamx.cli.path.CurrentDirectoryProvider;
import dev.streamx.mesh.mapper.MeshConfigMapper;
import dev.streamx.mesh.model.ServiceMesh;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;
import picocli.CommandLine.ParameterException;

@ApplicationScoped
class MeshDefinitionResolver {

  public static final String MESH_YAML = "mesh.yaml";
  public static final String MESH_YML = "mesh.yml";

  private final MeshConfigMapper mapper = new MeshConfigMapper();

  @Inject
  CommandLine.ParseResult parseResult;

  @Inject
  CurrentDirectoryProvider currentDirectoryProvider;

  @NotNull
  Path resolveMeshPath(MeshSource meshSource) {
    return Optional.ofNullable(meshSource)
        .map(source -> source.meshDefinitionFile)
        .map(Path::of)
        .orElseGet(this::resolveCurrentDirectoryMeshPath);
  }

  @NotNull
  MeshDefinitionResolver.MeshDefinition resolve(Path meshPath) throws IOException {
    return resolveMesh(meshPath);
  }

  @NotNull
  private Path resolveCurrentDirectoryMeshPath() {
    String currentDirectory = currentDirectoryProvider.provide();
    Path pathToYaml = Path.of(currentDirectory, MESH_YAML);
    Path pathToYml = Path.of(currentDirectory, MESH_YML);

    var yamlExists = pathToYaml.toFile().exists();
    var ymlExists = pathToYml.toFile().exists();

    if (yamlExists && ymlExists) {
      printf("Warning! Both '%s' and '%s' exist. Starting '%s' as it has higher priority.%n",
          pathToYaml, pathToYml, pathToYaml);
    }

    if (yamlExists) {
      return pathToYaml;
    } else if (ymlExists) {
      return pathToYml;
    } else {
      throw new ParameterException(parseResult.subcommand().commandSpec().commandLine(),
          "Missing mesh definition. Use '-f' to select mesh file or "
          + "make sure 'mesh.yaml' (or 'mesh.yml') exists in current directory.");
    }
  }

  @NotNull
  private MeshDefinition resolveMesh(Path pathToYaml) throws IOException {
    ServiceMesh serviceMesh = this.mapper.read(pathToYaml);
    return new MeshDefinition(pathToYaml, serviceMesh);
  }

  record MeshDefinition(Path path, ServiceMesh serviceMesh) {

  }
}
