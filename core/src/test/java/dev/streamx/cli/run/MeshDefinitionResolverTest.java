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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.ParseResult;

class MeshDefinitionResolverTest {

  private static final String TEST_MESH_LOCATION = "target/test-classes/mesh.yaml";
  private static final Path TEST_MESH_PATH = Path.of(TEST_MESH_LOCATION);

  private static final String CURRENT_DIRECTORY_MESH_YAML = "./mesh.yaml";
  private static final Path CURRENT_DIRECTORY_MESH_YAML_PATH = Path.of(CURRENT_DIRECTORY_MESH_YAML);

  private static final String CURRENT_DIRECTORY_MESH_YML = "./mesh.yml";
  private static final Path CURRENT_DIRECTORY_MESH_YML_PATH = Path.of(CURRENT_DIRECTORY_MESH_YML);

  MeshDefinitionResolver uut;

  @BeforeEach
  void setup() {
    uut = new MeshDefinitionResolver();
    uut.parseResult = getParseResult();
  }

  @AfterEach
  void tearDown() throws IOException {
    Files.deleteIfExists(CURRENT_DIRECTORY_MESH_YAML_PATH);
    Files.deleteIfExists(CURRENT_DIRECTORY_MESH_YML_PATH);
  }

  @Test
  void shouldResolveCurrentDirectoryMeshYaml() throws IOException {
    // given
    var meshYamlPath = CURRENT_DIRECTORY_MESH_YAML_PATH;
    Files.copy(
        TEST_MESH_PATH, meshYamlPath,
        StandardCopyOption.REPLACE_EXISTING);

    // when
    MeshDefinition result = uut.resolve(null);

    // then
    assertNotNull(result);
    assertThat(result.path()).isEqualTo(meshYamlPath);
    assertThat(result.serviceMesh()).isNotNull();
  }

  @Test
  void shouldPreferYamlOverYml() throws IOException {
    // given
    var meshYamlPath = CURRENT_DIRECTORY_MESH_YAML_PATH;
    Files.copy(
        TEST_MESH_PATH, meshYamlPath,
        StandardCopyOption.REPLACE_EXISTING);
    Files.copy(
        TEST_MESH_PATH, CURRENT_DIRECTORY_MESH_YML_PATH,
        StandardCopyOption.REPLACE_EXISTING);

    // when
    MeshDefinition result = uut.resolve(null);

    // then
    assertNotNull(result);
    assertThat(result.path()).isEqualTo(meshYamlPath);
    assertThat(result.serviceMesh()).isNotNull();
  }

  @Test
  void shouldResolveCurrentDirectoryMeshYml() throws IOException {
    // given
    var mesyYmlPath = CURRENT_DIRECTORY_MESH_YML_PATH;
    Files.copy(
        TEST_MESH_PATH, mesyYmlPath,
        StandardCopyOption.REPLACE_EXISTING);

    // when
    MeshDefinition result = uut.resolve(null);

    // then
    assertNotNull(result);
    assertThat(result.path()).isEqualTo(mesyYmlPath);
    assertThat(result.serviceMesh()).isNotNull();
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
    Files.deleteIfExists(CURRENT_DIRECTORY_MESH_YAML_PATH);
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
    meshSource.meshDefinitionFile = TEST_MESH_LOCATION;

    // when
    MeshDefinition result = uut.resolve(meshSource);

    // then
    assertNotNull(result);
    assertThat(result.path()).isEqualTo(TEST_MESH_PATH);
    assertThat(result.serviceMesh()).isNotNull();
  }
}