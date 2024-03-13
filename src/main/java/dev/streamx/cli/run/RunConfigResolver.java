package dev.streamx.cli.run;

import dev.streamx.cli.run.RunCommand.MeshSource;
import dev.streamx.runner.mapper.MeshConfigMapper;
import dev.streamx.runner.model.ServiceMesh;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.ParameterException;

@ApplicationScoped
class RunConfigResolver {

  private static final String CURRENT_DIRECTORY_MESH = "./streamx-mesh.yml";
  private static final String PREDEFINED_BLUEPRINT_CONFIG = "streamx-mesh.yml";

  private final MeshConfigMapper mapper = new MeshConfigMapper();

  @NotNull
  ConfigResolvingResult resolveConfig(MeshSource meshSource, CommandSpec spec) throws IOException {
    if (meshSource == null) {
      return resolveCurrentDirectoryConfig(spec);
    } else if (meshSource.blueprintsMesh) {
      return resolvePredefinedBlueprintMesh();
    } else if (meshSource.configFile != null) {
      return resolveExplicitlyGivenConfigFile(meshSource);
    } else {
      throw new ParameterException(spec.commandLine(), "StreamX config not found");
    }
  }

  @NotNull
  private ConfigResolvingResult resolveCurrentDirectoryConfig(CommandSpec spec)
      throws IOException {
    Path path;
    path = Path.of(CURRENT_DIRECTORY_MESH);
    if (path.toFile().exists()) {
      ServiceMesh serviceMesh = this.mapper.read(path);
      return new ConfigResolvingResult(path, serviceMesh);
    } else {
      throw new ParameterException(spec.commandLine(),
          "StreamX config not found in current directory");
    }
  }

  @NotNull
  private ConfigResolvingResult resolvePredefinedBlueprintMesh() {
    try (InputStream resourceAsStream = getClass().getClassLoader()
        .getResourceAsStream(PREDEFINED_BLUEPRINT_CONFIG);) {

      String config = new String(resourceAsStream.readAllBytes());
      ServiceMesh serviceMesh = this.mapper.read(config);

      return new ConfigResolvingResult(null, serviceMesh);
    } catch (Exception e) {
      throw new IllegalStateException("Predefined blueprint mesh not found...", e);
    }
  }

  @NotNull
  private ConfigResolvingResult resolveExplicitlyGivenConfigFile(MeshSource meshSource) throws IOException {
    Path path = Path.of(meshSource.configFile);
    ServiceMesh serviceMesh = this.mapper.read(path);

    return new ConfigResolvingResult(path, serviceMesh);
  }

  record ConfigResolvingResult(Path path, ServiceMesh serviceMesh) {

  }
}
