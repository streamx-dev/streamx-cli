package dev.streamx.cli.run;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import dev.streamx.cli.run.RunCommand.MeshSource;
import dev.streamx.cli.run.RunConfigResolver.ConfigResolvingResult;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.ParameterException;

class RunConfigResolverTest {

  private static final String DEFAULT_MESH_PATH = "target/classes/streamx-mesh.yml";
  public static final String CURRENT_DIRECTORY_MESH = "./streamx-mesh.yml";

  RunConfigResolver uut =   new RunConfigResolver();

  @Test
  void shouldResolveCurrentDirectoryMesh() throws IOException {
    try {
      // given
      Files.copy(
          Path.of(DEFAULT_MESH_PATH), Path.of(CURRENT_DIRECTORY_MESH),
          StandardCopyOption.REPLACE_EXISTING);

      // when
      ConfigResolvingResult result = uut.resolveConfig(null, getSpec());

      // then
      assertNotNull(result);
      assertThat(result.path()).isEqualTo(Path.of(CURRENT_DIRECTORY_MESH));
      assertThat(result.serviceMesh()).isNotNull();
    } finally {
      Files.delete(Path.of(CURRENT_DIRECTORY_MESH));
    }

  }

  private static CommandSpec getSpec() {
    CommandSpec mock = Mockito.mock();
    when(mock.commandLine()).thenReturn(Mockito.mock());
    return mock;
  }

  @Test
  void shouldThrowExceptionIfThereIsNoMeshInCurrentDirectory() throws IOException {
    // given
    Files.deleteIfExists(Path.of(CURRENT_DIRECTORY_MESH));
    MeshSource meshSource = new MeshSource();

    // when
    Exception exception = catchException(() -> uut.resolveConfig(meshSource, getSpec()));

    // then
    assertThat(exception).isInstanceOf(ParameterException.class);
    assertThat(exception).hasMessage("StreamX config not found");
  }

  @Test
  void shouldResolveBlueprintPredefined() throws IOException {
    // given
    MeshSource meshSource = new MeshSource();
    meshSource.blueprintsMesh = true;

    // when
    ConfigResolvingResult result = uut.resolveConfig(meshSource, getSpec());

    // then
    assertNotNull(result);
    assertThat(result.path()).isNull();
    assertThat(result.serviceMesh()).isNotNull();
  }

  @Test
  void shouldResolveGivenConfig() throws IOException {
    // given
    MeshSource meshSource = new MeshSource();
    meshSource.blueprintsMesh = false;
    meshSource.configFile = "target/classes/streamx-mesh.yml";

    // when
    ConfigResolvingResult result = uut.resolveConfig(meshSource, getSpec());

    // then
    assertNotNull(result);
    assertThat(result.path()).isEqualTo(Path.of("target/classes/streamx-mesh.yml"));
    assertThat(result.serviceMesh()).isNotNull();
  }
}