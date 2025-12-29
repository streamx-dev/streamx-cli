package dev.streamx.cli.command.meshprocessing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchException;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import dev.streamx.cli.path.CurrentDirectoryProvider;
import dev.streamx.cli.path.FixedCurrentDirectoryProvider;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.ParseResult;

class MeshResolverTest {

  private static final String TEST_MESH_LOCATION = "target/test-classes/mesh.yaml";
  private static final Path TEST_MESH_PATH = Path.of(TEST_MESH_LOCATION);

  private static final String MESH_YAML = "mesh.yaml";
  private static final String MESH_YML = "mesh.yml";

  MeshResolver uut;

  CurrentDirectoryProvider currentDirectoryProvider;

  @BeforeEach
  void setup(@TempDir Path tempDir) {
    uut = new MeshResolver();
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
    CommandSpec commandSpec = mock(CommandSpec.class);
    doReturn(mock(CommandLine.class)).when(commandSpec).commandLine();

    ParseResult parseResult = mock(ParseResult.class);
    doReturn(commandSpec).when(parseResult).commandSpec();
    doReturn(parseResult).when(parseResult).subcommand();

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
