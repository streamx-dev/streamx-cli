package dev.streamx.cli.run;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchException;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import dev.streamx.cli.path.CurrentDirectoryProvider;
import dev.streamx.cli.path.FixedCurrentDirectoryProvider;
import dev.streamx.cli.run.MeshDefinitionResolver.MeshDefinition;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.ParseResult;

class MeshDefinitionResolverTest {

  private static final String TEST_MESH_LOCATION = "target/test-classes/mesh.yaml";
  private static final Path TEST_MESH_PATH = Path.of(TEST_MESH_LOCATION);

  private static final String MESH_YAML = "mesh.yaml";
  private static final String MESH_YML = "mesh.yml";

  MeshDefinitionResolver uut;

  CurrentDirectoryProvider currentDirectoryProvider;

  @BeforeEach
  void setup(@TempDir Path tempDir) {
    uut = new MeshDefinitionResolver();
    currentDirectoryProvider = new FixedCurrentDirectoryProvider(tempDir);
    uut.currentDirectoryProvider = this.currentDirectoryProvider;
    uut.parseResult = getParseResult();
  }

  @AfterEach
  void tearDown() throws IOException {
    Files.deleteIfExists(currentDirectoryMeshYaml());
    Files.deleteIfExists(currentDirectoryMeshYml());
  }

  @Test
  void shouldResolveCurrentDirectoryMeshYaml() throws IOException {
    // given
    Files.copy(
        TEST_MESH_PATH, currentDirectoryMeshYaml(),
        StandardCopyOption.REPLACE_EXISTING);

    // when
    var result = uut.resolveMeshPath(null);

    // then
    assertNotNull(result);
    assertThat(result).isEqualTo(currentDirectoryMeshYaml());
  }

  @Test
  void shouldPreferYamlOverYml() throws IOException {
    // given
    Files.copy(
        TEST_MESH_PATH, currentDirectoryMeshYaml(),
        StandardCopyOption.REPLACE_EXISTING);
    Files.copy(
        TEST_MESH_PATH, currentDirectoryMeshYml(),
        StandardCopyOption.REPLACE_EXISTING);

    // when
    var result = uut.resolveMeshPath(null);

    // then
    assertNotNull(result);
    assertThat(result).isEqualTo(currentDirectoryMeshYaml());
  }

  @Test
  void shouldResolveCurrentDirectoryMeshYml() throws IOException {
    // given
    Files.copy(
        TEST_MESH_PATH, currentDirectoryMeshYml(),
        StandardCopyOption.REPLACE_EXISTING);

    // when
    var result = uut.resolveMeshPath(null);

    // then
    assertNotNull(result);
    assertThat(result).isEqualTo(currentDirectoryMeshYml());
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
  void shouldThrowExceptionIfThereIsNoMeshInCurrentDirectory() {
    // when
    Exception exception = catchException(() -> uut.resolveMeshPath(null));

    // then
    assertThat(exception).isInstanceOf(ParameterException.class);
    assertThat(exception).hasMessageContaining("Missing mesh definition");
  }

  @Test
  void shouldResolveGivenMeshDefinition() throws IOException {
    // when
    MeshDefinition result = uut.resolve(TEST_MESH_PATH);

    // then
    assertNotNull(result);
    assertThat(result.path()).isEqualTo(TEST_MESH_PATH);
    assertThat(result.serviceMesh()).isNotNull();
  }

  @NotNull
  private Path currentDirectoryMeshYaml() {
    var currentDir = currentDirectoryProvider.provide();
    return Path.of(currentDir, MESH_YAML);
  }

  @NotNull
  private Path currentDirectoryMeshYml() {
    var currentDir = currentDirectoryProvider.provide();
    return Path.of(currentDir, MESH_YML);
  }
}