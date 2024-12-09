package dev.streamx.cli.command.run;

import static org.assertj.core.api.Assertions.assertThat;

import dev.streamx.runner.event.MeshStarted;
import io.quarkus.runtime.ApplicationLifecycleManager;
import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainLauncher;
import io.quarkus.test.junit.main.QuarkusMainTest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

@QuarkusMainTest
public class RunCommandIT {

  @Test
  void shouldRunStreamxExampleMesh(QuarkusMainLauncher launcher) {
    String s = Paths.get("target/test-classes/mesh.yaml")
        .normalize()
        .toAbsolutePath()
        .toString();
    LaunchResult result = launcher.launch("run", "-f=" + s);

    assertThat(result.getOutput()).contains("STREAMX IS READY!");
  }

  @ApplicationScoped
  public static class Listener {

    void onMeshStarted(@Observes MeshStarted event) {
      ApplicationLifecycleManager.exit();
    }
  }
}
