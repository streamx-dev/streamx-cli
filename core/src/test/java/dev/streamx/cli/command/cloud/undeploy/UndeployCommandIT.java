package dev.streamx.cli.command.cloud.undeploy;

import static org.assertj.core.api.Assertions.assertThat;

import dev.streamx.cli.command.cloud.KubernetesClientProfile;
import dev.streamx.cli.command.cloud.ProjectUtils;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainLauncher;
import io.quarkus.test.junit.main.QuarkusMainTest;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

@QuarkusMainTest
@TestProfile(KubernetesClientProfile.class)
class UndeployCommandIT {

  @Test
  void shouldUndeployProject(QuarkusMainLauncher launcher) {
    String meshPath = ProjectUtils.getResourcePath(Path.of("with-configs.yaml")).toString();
    LaunchResult result = launcher.launch("undeploy", "-f=" + meshPath);
    assertThat(result.getOutput()).contains(
        "StreamX project successfully undeployed from 'default' namespace.");
  }
}