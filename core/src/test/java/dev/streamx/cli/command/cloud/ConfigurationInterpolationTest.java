package dev.streamx.cli.command.cloud;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import dev.streamx.operator.crd.ServiceMesh;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.nio.file.Path;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;

@QuarkusTest
class ConfigurationInterpolationTest {

  @Inject
  ServiceMeshResolver cut;

  @Test
  void shouldConfigurationBeInterpolatedFromProperties() {
    // Three properties overriding application.properties values
    System.setProperty("string.array.property", "stringArrayValue");
    System.setProperty("string.property", "stringValue");
    System.setProperty("integer.property", "0");
    // One extra property
    System.setProperty("boolean.property", "true");

    Path meshPath = ProjectUtils.getResourcePath(Path.of("configuration-interpolation.yaml"));
    ServiceMesh serviceMesh = cut.resolveMesh(meshPath);

    assertThat(serviceMesh.getSpec().getSources().get("cli").getOutgoing().get(0))
        .isEqualTo("stringArrayValue");
    assertThat(serviceMesh.getSpec().getProcessing().get("relay").getImage())
        .isEqualTo("stringValue");
    assertThat(serviceMesh.getSpec().getDeploymentConfig().getDelivery()
        .get("web-delivery-service").getReplicas()).isEqualTo(0);
    assertThat(serviceMesh.getSpec().getDeploymentConfig().getDelivery()
        .get("web-delivery-service").isStateful()).isTrue();
  }

  @Test
  void shouldConfigurationBeInterpolatedFromFile() {
    // Three properties comes from application.properties
    System.clearProperty("string.array.property");
    System.clearProperty("string.property");
    System.clearProperty("integer.property");
    // This one is declared only to let the mesh load
    System.setProperty("boolean.property", "true");

    Path meshPath = ProjectUtils.getResourcePath(Path.of("configuration-interpolation.yaml"));
    ServiceMesh serviceMesh = cut.resolveMesh(meshPath);

    assertThat(serviceMesh.getSpec().getSources().get("cli").getOutgoing().get(0))
        .isEqualTo("stringArrayValueFromFile");
    assertThat(serviceMesh.getSpec().getProcessing().get("relay").getImage())
        .isEqualTo("stringValueFromFile");
    assertThat(serviceMesh.getSpec().getDeploymentConfig().getDelivery()
        .get("web-delivery-service").getReplicas()).isEqualTo(1);
  }

  @Test
  void shouldConfigurationInterpolationFailOnMissingProperty() {
    System.clearProperty("boolean.property");

    final Path meshPath = ProjectUtils
        .getResourcePath(Path.of("configuration-interpolation.yaml"));

    RuntimeException ex = assertThrowsExactly(RuntimeException.class,
        () -> cut.resolveMesh(meshPath));
    assertThat(ex).hasRootCauseExactlyInstanceOf(NoSuchElementException.class);
  }

  @Test
  void shouldConfigurationInterpolationFailOnWrongType() {
    System.setProperty("boolean.property", "10");

    final Path meshPath = ProjectUtils
        .getResourcePath(Path.of("configuration-interpolation.yaml"));

    RuntimeException ex = assertThrowsExactly(RuntimeException.class,
        () -> cut.resolveMesh(meshPath));
    assertThat(ex).hasRootCauseExactlyInstanceOf(InvalidFormatException.class);
  }
}