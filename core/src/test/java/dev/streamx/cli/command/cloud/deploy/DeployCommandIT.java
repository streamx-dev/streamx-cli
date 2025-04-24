package dev.streamx.cli.command.cloud.deploy;

import static org.assertj.core.api.Assertions.assertThat;

import dev.streamx.cli.command.cloud.KubernetesClientProfile;
import dev.streamx.cli.command.cloud.ProjectUtils;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainLauncher;
import io.quarkus.test.junit.main.QuarkusMainTest;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

@QuarkusMainTest
@EnabledIf("dev.streamx.cli.OsUtils#isDockerAvailable")
@TestProfile(KubernetesClientProfile.class)
public class DeployCommandIT {

  @Test
  void shouldDeployProject(QuarkusMainLauncher launcher) {
    String meshPath = ProjectUtils.getResourcePath(Path.of("with-configs.yaml")).toString();
    LaunchResult result = launcher.launch("deploy", "-f=" + meshPath);
    assertThat(result.getOutput()).contains("successfully deployed to 'default' namespace.");
  }

}
