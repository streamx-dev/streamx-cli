package dev.streamx.cli.command.cloud.undeploy;

import static org.assertj.core.api.Assertions.assertThat;

import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainLauncher;
import io.quarkus.test.junit.main.QuarkusMainTest;
import org.junit.jupiter.api.Test;

@QuarkusMainTest
class UndeployCommandIT {

  @Test
  void shouldUndeployProject(QuarkusMainLauncher launcher) {
    LaunchResult result = launcher.launch("undeploy");
    assertThat(result.getOutput()).contains(
        "StreamX project successfully undeployed from 'default' namespace.");
  }
}