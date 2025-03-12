package dev.streamx.cli.command.dev;

import com.github.dockerjava.api.DockerClient;
import dev.streamx.runner.validation.DockerContainerValidator;
import dev.streamx.runner.validation.DockerEnvironmentValidator;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import java.util.Set;
import org.jboss.logging.Logger;

@Dependent
public class DockerValidator {

  private static final Logger LOG = Logger.getLogger(DockerValidator.class);

  @Inject
  DockerEnvironmentValidator dockerEnvironmentValidator;

  @Inject
  DockerContainerValidator dockerContainerValidator;

  public void validateDockerEnvironment(Set<String> validatedContainerNames) {
    LOG.info("Validating environment...");

    DockerClient client = dockerEnvironmentValidator.validateDockerClient();
    dockerContainerValidator.verifyExistingContainers(client, validatedContainerNames);
  }
}
