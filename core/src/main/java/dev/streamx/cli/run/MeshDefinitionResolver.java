package dev.streamx.cli.run;

import static dev.streamx.cli.util.Output.printf;

import dev.streamx.cli.run.RunCommand.MeshSource;
import dev.streamx.mesh.mapper.MeshConfigMapper;
import dev.streamx.mesh.model.ServiceMesh;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.IOException;
import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;
import picocli.CommandLine.ParameterException;

@ApplicationScoped
class MeshDefinitionResolver {

  public static final String CURRENT_DIRECTORY_MESH_YAML = "./mesh.yaml";
  public static final String CURRENT_DIRECTORY_MESH_YML = "./mesh.yml";

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
    Path pathToYaml = Path.of(CURRENT_DIRECTORY_MESH_YAML);
    Path pathToYml = Path.of(CURRENT_DIRECTORY_MESH_YML);

    var yamlExists = pathToYaml.toFile().exists();
    var ymlExists = pathToYml.toFile().exists();

    if (yamlExists && ymlExists) {
      printf("Warning! Both '%s' and '%s' exist. Starting '%s' as it has higher priority.",
          CURRENT_DIRECTORY_MESH_YAML, CURRENT_DIRECTORY_MESH_YML, CURRENT_DIRECTORY_MESH_YAML);
    }

    if (yamlExists) {
      return resolveMesh(pathToYaml);
    } else if (ymlExists) {
      return resolveMesh(pathToYml);
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

  @NotNull
  private MeshDefinitionResolver.MeshDefinition resolveExplicitlyGivenMeshDefinitionFile(
      MeshSource meshSource) throws IOException {
    Path path = Path.of(meshSource.meshDefinitionFile);
    return resolveMesh(path);
  }

  record MeshDefinition(Path path, ServiceMesh serviceMesh) {

  }
}
