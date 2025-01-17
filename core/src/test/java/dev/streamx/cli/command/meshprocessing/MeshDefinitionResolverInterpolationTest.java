package dev.streamx.cli.command.meshprocessing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.io.IOException;
import java.nio.file.Path;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.JsonMappingException;

@QuarkusTest
class MeshDefinitionResolverInterpolationTest {

  private static final String TEST_MESH_LOCATION = "target/test-classes/mesh-interpolated.yaml";
  private static final Path TEST_MESH_PATH = Path.of(TEST_MESH_LOCATION);

  @Inject
  MeshDefinitionResolver uut;

  @Test
  void shouldFailWithPropertyUndefined() {
    System.clearProperty("config.image.interpolated");

    JsonMappingException ex = assertThrowsExactly(JsonMappingException.class,
        () -> uut.resolve(TEST_MESH_PATH));
    assertThat(ex).hasRootCauseExactlyInstanceOf(NoSuchElementException.class);
  }
  @Test
  void shouldResolveWithPropertyDefined() throws IOException {
    System.setProperty("config.image.interpolated", "value");

    var result = uut.resolve(TEST_MESH_PATH);

    assertThat(result).isNotNull();
  }
}
