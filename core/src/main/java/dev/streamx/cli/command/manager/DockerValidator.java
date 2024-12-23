package dev.streamx.cli.command.manager;

import static dev.streamx.runner.StreamxRunner.MESH_FILE_PATH_LABEL;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.ListContainersCmd;
import com.github.dockerjava.api.model.Container;
import dev.streamx.runner.validation.excpetion.DockerContainerNonUniqueException;
import dev.streamx.runner.validation.excpetion.DockerContainerNonUniqueException.ContainerStatus;
import dev.streamx.runner.validation.excpetion.DockerEnvironmentException;
import jakarta.enterprise.context.Dependent;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;
import org.jetbrains.annotations.NotNull;
import org.testcontainers.DockerClientFactory;

@Dependent
public class DockerValidator {

  private static final Logger LOG = Logger.getLogger(DockerValidator.class);

  public void validateDockerEnvironment(Set<String> validatedContainerNames) {
    LOG.info("Validating environment...");

    DockerClient client = retrieveDockerClient();
    verifyExistingContainers(client, validatedContainerNames);
  }

  private static void verifyExistingContainers(DockerClient client,
      Set<String> validatedContainerNames) {
    ListContainersCmd listContainersCmd = client.listContainersCmd()
        .withNameFilter(validatedContainerNames)
        .withShowAll(true);
    List<Container> containers = listContainersCmd.exec();

    Set<ContainerStatus> containerStatuses = containers.stream()
        .filter(c -> containerHasNameFromValidated(validatedContainerNames, c))
        .map(c -> new ContainerStatus(
            trimSlashesFromContainerNames(c.getNames()).stream()
                .collect(Collectors.joining(", ")),
            c.getState(),
            c.getLabels().get(MESH_FILE_PATH_LABEL))
        )
        .collect(Collectors.toSet());

    if (!containerStatuses.isEmpty()) {
      throw new DockerContainerNonUniqueException(containerStatuses);
    }
  }

  private static boolean containerHasNameFromValidated(
      Set<String> validatedContainerNames, Container c) {
    List<String> namesWithTrimmedSlash = trimSlashesFromContainerNames(c.getNames());

    return namesWithTrimmedSlash.stream()
        .anyMatch(name -> validatedContainerNames.stream()
            .anyMatch(name::equalsIgnoreCase)
        );
  }

  @NotNull
  private static List<String> trimSlashesFromContainerNames(String[] names) {
    return Arrays.stream(names)
        .map(DockerValidator::trimSlash)
        .sorted()
        .toList();
  }

  private static String trimSlash(String name) {
    return StringUtils.startsWith(name, "/")
        ? name.substring("/".length())
        : name;
  }

  private static DockerClient retrieveDockerClient() {
    try {
      return DockerClientFactory.instance().client();
    } catch (Exception e) {
      throw new DockerEnvironmentException(e);
    }
  }
}
