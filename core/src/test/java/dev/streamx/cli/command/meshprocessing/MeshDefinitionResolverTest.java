package dev.streamx.cli.command.meshprocessing;

import static org.assertj.core.api.Assertions.assertThat;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.io.IOException;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

@QuarkusTest
class MeshDefinitionResolverTest {

  private static final String TEST_MESH_LOCATION = "target/test-classes/mesh.yaml";
  private static final Path TEST_MESH_PATH = Path.of(TEST_MESH_LOCATION);

  @Inject
  MeshDefinitionResolver uut;

  @Test
  void shouldResolveGivenMeshDefinition() throws IOException {
    // when
    var result = uut.resolve(TEST_MESH_PATH);

    // then
    assertThat(result).isNotNull();
  }
}
