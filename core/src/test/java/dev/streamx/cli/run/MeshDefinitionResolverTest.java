package dev.streamx.cli.run;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchException;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import dev.streamx.cli.run.MeshDefinitionResolver.MeshDefinition;
import dev.streamx.cli.run.RunCommand.MeshSource;
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

  private static final String TEST_MESH_PATH = "target/test-classes/mesh.yml";
  private static final String CURRENT_DIRECTORY_MESH = "./mesh.yml";

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
          Path.of(TEST_MESH_PATH), Path.of(CURRENT_DIRECTORY_MESH),
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
  void shouldResolveGivenMeshDefinition() throws IOException {
    // given
    MeshSource meshSource = new MeshSource();
    meshSource.meshDefinitionFile = TEST_MESH_PATH;

    // when
    MeshDefinition result = uut.resolve(meshSource);

    // then
    assertNotNull(result);
    assertThat(result.path()).isEqualTo(Path.of(TEST_MESH_PATH));
    assertThat(result.serviceMesh()).isNotNull();
  }
}