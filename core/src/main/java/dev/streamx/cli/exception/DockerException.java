package dev.streamx.cli.exception;

import dev.streamx.runner.validation.excpetion.DockerContainerNonUniqueException.ContainerStatus;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.testcontainers.shaded.org.apache.commons.lang3.StringUtils;

public class DockerException extends RuntimeException {

  private DockerException(String message, Exception exception) {
    super(message, exception);
  }

  private DockerException(String message) {
    super(message);
  }

  public static DockerException dockerEnvironmentException() {
    return new DockerException("""
        Could not find a valid Docker environment.

        Make sure that:
         * Docker is installed,
         * Docker is running""");
  }

  public static DockerException nonUniqueContainersException(
      List<ContainerStatus> containerStatus) {
    Optional<String> commonMesh = calculateCommonMeshForAllContainers(containerStatus);

    return commonMesh
        .map(DockerException::nonUniqueContainersWithCommonMesh)
        .orElseGet(() -> genericNonUniqueContainers(containerStatus));
  }

  @NotNull
  private static DockerException genericNonUniqueContainers(List<ContainerStatus> containerStatus) {
    String conflictingContainersFragment = generateConflictingContainersFragment(containerStatus);

    String pluralMessageVersion = """
        StreamX tries to start Docker containers. It looks like
        %s
        names are already in use. \
        Remove or rename the containers with these names before restarting StreamX Mesh.""";
    String singularMessageVersion = """
        StreamX tries to start Docker containers. It looks like
        %s
        name is already in use. \
        Remove or rename the container with this name before restarting StreamX Mesh.""";

    String template = containerStatus.size() > 1
        ? pluralMessageVersion
        : singularMessageVersion;
    return new DockerException(template.formatted(conflictingContainersFragment));
  }

  @NotNull
  private static DockerException nonUniqueContainersWithCommonMesh(String commonMesh) {
    String message = """
        It looks like some StreamX Mesh is already running. \
        Mesh definition:
        %s

        Stop the running mesh \
        or remove containers of the running Mesh before starting new StreamX Mesh."""
        .formatted(commonMesh);

    return new DockerException(message);
  }

  @NotNull
  private static Optional<String> calculateCommonMeshForAllContainers(
      List<ContainerStatus> containerStatus) {
    boolean allNonuniqueContainersAreFromMesh = containerStatus.stream()
        .allMatch(cs -> StringUtils.isNotBlank(cs.meshPath()));

    Set<String> meshNames = containerStatus.stream()
        .map(ContainerStatus::meshPath)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());

    if (allNonuniqueContainersAreFromMesh && meshNames.size() == 1) {
      return meshNames.stream().findFirst();
    }
    return Optional.empty();
  }

  @NotNull
  private static String generateConflictingContainersFragment(
      List<ContainerStatus> containerStatus) {
    return containerStatus.stream()
        .map(cs -> " * " + removeSlashPrefix(cs.name()))
        .collect(Collectors.joining("\n"));
  }

  @NotNull
  private static String removeSlashPrefix(String cs) {
    return cs.startsWith("/") ? cs.substring(1) : cs;
  }
}
