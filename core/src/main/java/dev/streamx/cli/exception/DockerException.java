package dev.streamx.cli.exception;

import dev.streamx.runner.validation.excpetion.DockerContainerNonUniqueException.ContainerStatus;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public class DockerException extends RuntimeException {

  private DockerException(String message, Exception exception) {
    super(message, exception);
  }

  private DockerException(String message) {
    super(message);
  }

  public static DockerException dockerEnvironmentException() {
    return new DockerException("""
        Could not find a valid Docker environment. Check logs for details.

        Make sure that:
         * Docker is installed,
         * Docker is running""");
  }

  public static DockerException nonUniqueContainersException(
      List<ContainerStatus> containerStatus) {
    String runningContainersFragment = generateRunningContainersFragment(containerStatus);
    String nonRunningContainersToRemove = generateContainersToRemoveFragment(containerStatus);

    String template = """
        StreamX needs to start containers, but there are already containers with the same names.
        
        %s%s"""
        .formatted(runningContainersFragment, nonRunningContainersToRemove)
        .trim();

    return new DockerException(template);
  }

  @NotNull
  private static String generateContainersToRemoveFragment(List<ContainerStatus> containerStatus) {
    List<ContainerStatus> nonRunningContainers = containerStatus.stream()
        .filter(cs -> !"running".equals(cs.state()))
        .toList();

    String nonRunningContainersAsString = nonRunningContainers.stream()
        .map(cs -> " * " + cs.name() + " in status " + cs.state() + " " + fromMeshFragment(cs))
        .collect(Collectors.joining("\n"));

    return !nonRunningContainers.isEmpty()
        ? "Please remove these containers:\n"
            + nonRunningContainersAsString
            + "\n"
        : "";
  }

  @NotNull
  private static String generateRunningContainersFragment(List<ContainerStatus> containerStatus) {
    List<ContainerStatus> runningContainers = containerStatus.stream()
        .filter(cs -> "running".equals(cs.state()))
        .toList();

    String runningContainersAsString = runningContainers.stream()
        .map(cs -> " * " + cs.name() + " " + fromMeshFragment(cs))
        .collect(Collectors.joining("\n"));

    return !runningContainers.isEmpty()
        ? "Maybe you already launched StreamX mesh? "
            + "Check running containers:\n"
            + runningContainersAsString
            + "\n"
        : "";
  }

  @NotNull
  private static String fromMeshFragment(ContainerStatus cs) {
    return cs.meshPath() != null ? "(from mesh " + cs.meshPath() + ")" : "";
  }
}
