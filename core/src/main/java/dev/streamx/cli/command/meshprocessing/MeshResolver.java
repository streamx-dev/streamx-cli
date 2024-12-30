package dev.streamx.cli.command.meshprocessing;

import static dev.streamx.cli.util.Output.printf;

import dev.streamx.cli.path.CurrentDirectoryProvider;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.nio.file.Path;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;
import picocli.CommandLine.ParameterException;

@ApplicationScoped
public class MeshResolver {

  public static final String MESH_YAML = "mesh.yaml";
  public static final String MESH_YML = "mesh.yml";

  @Inject
  CommandLine.ParseResult parseResult;

  @Inject
  CurrentDirectoryProvider currentDirectoryProvider;

  @NotNull
  public Path resolveMeshPath(MeshSource meshSource) {
    return Optional.ofNullable(meshSource)
        .map(source -> source.meshDefinitionFile)
        .map(Path::of)
        .orElseGet(this::resolveCurrentDirectoryMeshPath);
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
}
