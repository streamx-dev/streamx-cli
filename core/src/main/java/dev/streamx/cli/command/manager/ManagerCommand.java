package dev.streamx.cli.command.manager;

import static dev.streamx.cli.util.Output.print;

import dev.streamx.cli.BannerPrinter;
import dev.streamx.cli.VersionProvider;
import dev.streamx.cli.command.manager.event.MeshManagerStarted;
import dev.streamx.cli.command.meshprocessing.MeshResolver;
import dev.streamx.cli.command.meshprocessing.MeshSource;
import dev.streamx.cli.exception.DockerException;
import dev.streamx.cli.util.ExceptionUtils;
import dev.streamx.cli.util.StreamxMavenPropertiesUtils;
import dev.streamx.runner.validation.excpetion.DockerContainerNonUniqueException;
import dev.streamx.runner.validation.excpetion.DockerEnvironmentException;
import io.quarkus.logging.Log;
import io.quarkus.runtime.Quarkus;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import java.time.Duration;
import java.util.Set;
import org.rnorth.ducttape.TimeoutException;
import org.testcontainers.containers.ContainerLaunchException;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;

@Command(name = ManagerCommand.COMMAND_NAME,
    mixinStandardHelpOptions = true,
    versionProvider = VersionProvider.class,
    hidden = true, // FIXME change after official release of StreamX Mesh Manager
    description = "Serves StreamX Mesh Manager locally.")
public class ManagerCommand implements Runnable {

  public static final String COMMAND_NAME = "manager";

  private static final long CONTAINER_TIMEOUT_IN_SECS = 60_000L;

  @ArgGroup
  MeshSource meshSource;

  @Inject
  MeshResolver meshResolver;

  @Inject
  BannerPrinter bannerPrinter;

  @Inject
  ManagerConfig managerConfig;

  @Inject
  DockerValidator dockerValidator;

  @Inject
  Event<MeshManagerStarted> meshManagerStartedEvent;

  @Override
  public void run() {
    try {
      dockerValidator.validateDockerEnvironment(Set.of(MeshManagerContainer.CONTAINER_NAME));

      var meshPath = meshResolver.resolveMeshPath(meshSource);
      var meshPathAsString = meshPath.toAbsolutePath().normalize().toString();
      var projectDirectory = meshPath.resolve("..");
      var projectDirectoryAsString = projectDirectory.toAbsolutePath().normalize().toString();

      Log.infov("Resolved mesh {0} and project directory {1}",
          meshPathAsString, projectDirectoryAsString);

      bannerPrinter.printBanner();

      print("Setting up StreamX Mesh Manager...");

      startMeshManager(meshPathAsString, projectDirectoryAsString);
    } catch (ContainerLaunchException e) {
      if (org.apache.commons.lang3.exception.ExceptionUtils
          .throwableOfThrowable(e.getCause(), TimeoutException.class) != null) {
        throw DockerException.containerStartupFailed(
            MeshManagerContainer.CONTAINER_NAME, CONTAINER_TIMEOUT_IN_SECS);
      }
      throw throwGenericException(e);
    } catch (DockerContainerNonUniqueException e) {
      throw DockerException.nonUniqueContainersException(e.getContainers());
    } catch (DockerEnvironmentException e) {
      throw DockerException.dockerEnvironmentException();
    } catch (Exception e) {
      throw throwGenericException(e);
    }
  }

  private void startMeshManager(String meshPathAsString, String projectDirectoryAsString) {
    try (var meshManagerContainer = new MeshManagerContainer(
        StreamxMavenPropertiesUtils.getMeshManagerImage(),
        managerConfig.meshManagerPort(),
        meshPathAsString,
        projectDirectoryAsString
    ).withStartupTimeout(Duration.ofSeconds(CONTAINER_TIMEOUT_IN_SECS))) {
      meshManagerContainer.start();

      print("StreamX Mesh Manager started on http://localhost:" + managerConfig.meshManagerPort());

      meshManagerStartedEvent.fire(new MeshManagerStarted());

      Quarkus.waitForExit();
    }
  }

  private RuntimeException throwGenericException(Exception e) {
    return new RuntimeException(
        ExceptionUtils.appendLogSuggestion(
            "Unable serve MeshManager.\n"
                + "\n"
                + "Details:\n"
                + e.getMessage()), e);
  }
}
