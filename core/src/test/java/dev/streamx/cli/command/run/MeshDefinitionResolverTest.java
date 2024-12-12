package dev.streamx.cli.command.run;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import dev.streamx.cli.command.run.MeshDefinitionResolver.MeshDefinition;
import dev.streamx.cli.path.CurrentDirectoryProvider;
import dev.streamx.cli.path.FixedCurrentDirectoryProvider;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import picocli.CommandLine.Model.CommandSpec;
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

  private static ParseResult getParseResult() {

    CommandSpec commandSpec = Mockito.mock();
    when(commandSpec.commandLine()).thenReturn(Mockito.mock());

    ParseResult parseResult = Mockito.mock();
    when(parseResult.commandSpec()).thenReturn(commandSpec);
    when(parseResult.subcommand()).thenReturn(parseResult);

    return parseResult;
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
