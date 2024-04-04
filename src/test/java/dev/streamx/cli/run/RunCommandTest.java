package dev.streamx.cli.run;

import static org.assertj.core.api.Assertions.assertThat;

import dev.streamx.cli.run.util.StreamxRunManager;
import dev.streamx.cli.run.util.StreamxRunManager.StreamxRunTestProfile;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainLauncher;
import io.quarkus.test.junit.main.QuarkusMainTest;
import org.junit.jupiter.api.Test;

@QuarkusMainTest
@TestProfile(StreamxRunTestProfile.class)
public class RunCommandTest {

  @Test
  void shouldRunStreamXBlueprintMesh(QuarkusMainLauncher launcher) {
    StreamxRunManager streamxRunManager = StreamxRunManager.of(launcher, "run", "--blueprints-mesh");

    streamxRunManager.start();
    streamxRunManager.shutdown();
    LaunchResult result = streamxRunManager.fetchResult();

    assertThat(result.getOutput()).contains("STREAMX IS READY!");
  }

  @Test
  void shouldRunStreamXSecuredMesh(QuarkusMainLauncher launcher) {
    StreamxRunManager streamxRunManager = StreamxRunManager.of(launcher,
        "run", "-f", "target/test-classes/secured-streamx-mesh.yml");

    streamxRunManager.start();
    streamxRunManager.shutdown();
    LaunchResult result = streamxRunManager.fetchResult();

    assertThat(result.getOutput()).contains("STREAMX IS READY!");
  }
}