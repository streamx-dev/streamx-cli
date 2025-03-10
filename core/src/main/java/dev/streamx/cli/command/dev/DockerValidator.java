package dev.streamx.cli.command.dev;

import com.github.dockerjava.api.DockerClient;
import dev.streamx.runner.validation.DockerContainerValidator;
import dev.streamx.runner.validation.DockerEnvironmentValidator;
import dev.streamx.runner.validation.excpetion.DockerEnvironmentException;
import jakarta.enterprise.context.Dependent;
import java.util.Set;
import org.jboss.logging.Logger;
import org.testcontainers.DockerClientFactory;

@Dependent
public class DockerValidator {

  private static final Logger LOG = Logger.getLogger(DockerValidator.class);

  public void validateDockerEnvironment(Set<String> validatedContainerNames) {
    LOG.info("Validating environment...");

    DockerEnvironmentValidator.validateDockerClient();
    DockerClient client = retrieveDockerClient();
    DockerContainerValidator.verifyExistingContainers(client, validatedContainerNames);
  }

  private static DockerClient retrieveDockerClient() {
    try {
      return DockerClientFactory.instance().client();
    } catch (Exception e) {
      throw new DockerEnvironmentException(e);
    }
  }
}
