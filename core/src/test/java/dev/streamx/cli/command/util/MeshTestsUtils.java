package dev.streamx.cli.command.util;

import com.github.dockerjava.api.DockerClient;
import dev.streamx.runner.validation.DockerContainerValidator;
import dev.streamx.runner.validation.DockerEnvironmentValidator;
import java.util.Set;

public class MeshTestsUtils {
  public static void cleanUpMesh(Set<String> cleanedUpContainers) {
    DockerClient client = new DockerEnvironmentValidator().validateDockerClient();
    for (String container : cleanedUpContainers) {
      try {
        client.removeContainerCmd(container)
            .withForce(true)
            .exec();
      } catch (Exception ignored) {
        // Ignore
      }
    }
    new DockerContainerValidator().verifyExistingContainers(client, cleanedUpContainers);
  }
}
