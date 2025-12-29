package dev.streamx.cli.command.util;

import com.github.dockerjava.api.DockerClient;
import com.streamx.runner.validation.DockerContainerValidator;
import com.streamx.runner.validation.DockerEnvironmentValidator;
import java.util.Set;

public class MeshTestsUtils {
  public static void cleanUpMesh(String... containersToRemove) {
    DockerClient client = new DockerEnvironmentValidator().validateDockerClient();
    for (String container : containersToRemove) {
      try {
        client.removeContainerCmd(container)
            .withForce(true)
            .exec();
      } catch (Exception ignored) {
        // Ignore
      }
    }
    new DockerContainerValidator().verifyExistingContainers(client, Set.of(containersToRemove));
  }
}
