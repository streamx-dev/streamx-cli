package dev.streamx.cli.command.run;

import static org.assertj.core.api.Assertions.assertThat;

import dev.streamx.cli.command.MeshStopper;
import dev.streamx.cli.command.run.RunCommandTest.RunCommandProfile;
import dev.streamx.runner.event.MeshStarted;
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
import org.junit.jupiter.api.Test;

@QuarkusMainTest
@TestProfile(RunCommandProfile.class)
public class RunCommandTest {

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
