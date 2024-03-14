package dev.streamx.cli.run;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import dev.streamx.cli.run.RunCommand.MeshSource;
import dev.streamx.cli.run.MeshDefinitionResolver.MeshDefinition;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.ParseResult;

class MeshDefinitionResolverTest {

  private static final String DEFAULT_MESH_PATH = "target/classes/streamx-mesh.yml";
  private static final String CURRENT_DIRECTORY_MESH = "./streamx-mesh.yml";

  MeshDefinitionResolver uut;

  @BeforeEach
  void setup() {
    uut = new MeshDefinitionResolver();
    uut.parseResult = getParseResult();
  }

  @Test
  void shouldResolveCurrentDirectoryMesh() throws IOException {
    try {
      // given
      Files.copy(
          Path.of(DEFAULT_MESH_PATH), Path.of(CURRENT_DIRECTORY_MESH),
          StandardCopyOption.REPLACE_EXISTING);

      // when
      MeshDefinition result = uut.resolve(null);

      // then
      assertNotNull(result);
      assertThat(result.path()).isEqualTo(Path.of(CURRENT_DIRECTORY_MESH));
      assertThat(result.serviceMesh()).isNotNull();
    } finally {
      Files.delete(Path.of(CURRENT_DIRECTORY_MESH));
    }

  }

  private static ParseResult getParseResult() {

    CommandSpec commandSpec = Mockito.mock();
    when(commandSpec.commandLine()).thenReturn(Mockito.mock());

    ParseResult parseResult = Mockito.mock();
    when(parseResult.commandSpec()).thenReturn(commandSpec);
    when(parseResult.subcommand()).thenReturn(parseResult);

    return parseResult;
  }

  @Test
  void shouldThrowExceptionIfThereIsNoMeshInCurrentDirectory() throws IOException {
    // given
    Files.deleteIfExists(Path.of(CURRENT_DIRECTORY_MESH));
    MeshSource meshSource = new MeshSource();

    // when
    Exception exception = catchException(() -> uut.resolve(meshSource));

    // then
    assertThat(exception).isInstanceOf(ParameterException.class);
    assertThat(exception).hasMessage("Mesh definition not found");
  }

  @Test
  void shouldResolveBlueprintPredefined() throws IOException {
    // given
    MeshSource meshSource = new MeshSource();
    meshSource.blueprintsMesh = true;

    // when
    MeshDefinition result = uut.resolve(meshSource);

    // then
    assertNotNull(result);
    assertThat(result.path()).isNull();
    assertThat(result.serviceMesh()).isNotNull();
  }

  @Test
  void shouldResolveGivenMeshDefinition() throws IOException {
    // given
    MeshSource meshSource = new MeshSource();
    meshSource.blueprintsMesh = false;
    meshSource.meshDefinitionFile = "target/classes/streamx-mesh.yml";

    // when
    MeshDefinition result = uut.resolve(meshSource);

    // then
    assertNotNull(result);
    assertThat(result.path()).isEqualTo(Path.of("target/classes/streamx-mesh.yml"));
    assertThat(result.serviceMesh()).isNotNull();
  }
}