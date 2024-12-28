package dev.streamx.cli.command.cloud.deploy;

import static org.assertj.core.api.Assertions.assertThat;

import dev.streamx.cli.command.cloud.ProjectUtils;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.NonDeletingOperation;
import io.quarkus.arc.properties.IfBuildProperty;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainLauncher;
import io.quarkus.test.junit.main.QuarkusMainTest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

@QuarkusMainTest
@TestProfile(KubernetesClientProfile.class)
public class DeployCommandIT {

  @ApplicationScoped
  @IfBuildProperty(name = "%test.quarkus.kubernetes-client.devservices.enabled",
      stringValue = "true")
  public static class Listener {

    @Inject
    KubernetesClient kubernetesClient;

    void onStart(@Observes StartupEvent ev) {
      kubernetesClient.apiextensions().v1()
          .customResourceDefinitions()
          .load(DeployCommandIT.class.getResourceAsStream("servicemeshes.streamx.dev-v1.yml"))
          .createOr(NonDeletingOperation::update);
    }
  }

  @Test
  void shouldDeployProject(QuarkusMainLauncher launcher) {
    String meshPath = ProjectUtils.getMeshPath("with-configs.yaml").toString();
    LaunchResult result = launcher.launch("cloud", "deploy", "-f=" + meshPath);

    assertThat(result.getOutput()).contains("successfully deployed to 'default' namespace.");
  }

}
