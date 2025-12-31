package dev.streamx.cli.command.meshprocessing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.streamx.mesh.model.ServiceMesh;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.io.IOException;
import java.nio.file.Path;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class MeshDefinitionResolverInterpolationTest {

  private static final String TEST_MESH_LOCATION = "src/test/resources/filtered/mesh-interpolated.yaml";
  private static final Path TEST_MESH_PATH = Path.of(TEST_MESH_LOCATION);

  @Inject
  MeshDefinitionResolver uut;

  @BeforeEach
  void clearSystemProperties() {
    System.clearProperty("config.image.interpolated");
    System.clearProperty("config.source.interpolated");
  }

  @Test
  void shouldFailWithMandatoryPropertyUndefined() {
    assertThatThrownBy(() -> uut.resolve(TEST_MESH_PATH))
        .isInstanceOf(JsonMappingException.class)
        .hasRootCauseInstanceOf(NoSuchElementException.class)
        .hasRootCauseMessage("Could not expand value config.image.interpolated"
                             + " in expression ${config.image.interpolated}");
  }

  @Test
  void shouldResolveWithMandatoryPropertyDefinedAndOptionalPropertyUndefined() throws IOException {
    System.setProperty("config.image.interpolated", "image-1");

    ServiceMesh result = uut.resolve(TEST_MESH_PATH);
    assertSinkImage(result, "image-1");
    assertSourceRef(result, "inbox.pages");
  }

  @Test
  void shouldResolveWithMandatoryAndOptionalPropertiesDefined() throws IOException {
    System.setProperty("config.image.interpolated", "image-1");
    System.setProperty("config.source.interpolated", "source-1");

    ServiceMesh result = uut.resolve(TEST_MESH_PATH);
    assertSinkImage(result, "image-1");
    assertSourceRef(result, "source-1");
  }

  private static void assertSinkImage(ServiceMesh result, String expected) {
    String actual = result
        .getDescriptors()
        .get("web-server-sink")
        .getContainers()
        .get("sink")
        .getImage();
    assertThat(actual).isEqualTo(expected);
  }

  private static void assertSourceRef(ServiceMesh result, String expected) {
    String actual = result
        .getSources()
        .get("cli")
        .getOutgoing()
        .getFirst()
        .getRef();
    assertThat(actual).isEqualTo(expected);
  }
}
