package dev.streamx.cli.command.run;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.dockerjava.api.DockerClient;
import dev.streamx.cli.command.MeshStopper;
import dev.streamx.cli.command.run.RunCommandTest.RunCommandProfile;
import dev.streamx.runner.event.MeshStarted;
import dev.streamx.runner.validation.DockerContainerValidator;
import dev.streamx.runner.validation.DockerEnvironmentValidator;
import io.quarkus.arc.properties.IfBuildProperty;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainLauncher;
import io.quarkus.test.junit.main.QuarkusMainTest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

@QuarkusMainTest
@EnabledIf("dev.streamx.cli.OsUtils#isDockerAvailable")
@TestProfile(RunCommandProfile.class)
public class RunCommandTest {

  @AfterEach
  void awaitDockerResourcesAreRemoved() {
    Awaitility.await()
        .until(() -> {
          try {
            Set<String> cleanedUpContainers =
                Set.of("pulsar", "pulsar-init",
                    "rest-ingestion", "relay", "web-delivery-service");
            DockerClient client = new DockerEnvironmentValidator().validateDockerClient();
            new DockerContainerValidator().verifyExistingContainers(client, cleanedUpContainers);

            return true;
          } catch (Exception e) {
            return false;
          }
        });
  }

  @Test
  void shouldRunStreamxExampleMesh(QuarkusMainLauncher launcher) {
    String s = Paths.get("target/test-classes/mesh.yaml")
        .toAbsolutePath()
        .normalize()
        .toString();
    LaunchResult result = launcher.launch("run", "-f=" + s);

    assertThat(result.getOutput()).contains("STREAMX IS READY!");
  }

  @ApplicationScoped
  @IfBuildProperty(name = "streamx.run.test.profile", stringValue = "true")
  public static class Listener {

    @Inject
    MeshStopper meshStopper;

    void onMeshStarted(@Observes MeshStarted event) {
      meshStopper.scheduleStop();
    }
  }

  public static class RunCommandProfile implements QuarkusTestProfile {

    @Override
    public Map<String, String> getConfigOverrides() {
      return Map.of("streamx.run.test.profile", "true");
    }
  }
}
