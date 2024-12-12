package dev.streamx.cli.command.manage;

import static dev.streamx.cli.util.Output.print;

import dev.streamx.cli.BannerPrinter;
import dev.streamx.cli.VersionProvider;
import dev.streamx.cli.command.manage.event.MeshManagerStarted;
import dev.streamx.cli.command.meshprocessing.MeshResolver;
import dev.streamx.cli.command.meshprocessing.MeshSource;
import dev.streamx.cli.exception.DockerException;
import dev.streamx.cli.util.ExceptionUtils;
import dev.streamx.runner.exception.ContainerStartupTimeoutException;
import dev.streamx.runner.validation.excpetion.DockerContainerNonUniqueException;
import dev.streamx.runner.validation.excpetion.DockerEnvironmentException;
import io.quarkus.runtime.Quarkus;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import java.nio.file.Path;
import java.util.Set;
import org.jboss.logging.Logger;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;

@Command(name = ManageCommand.COMMAND_NAME,
    mixinStandardHelpOptions = true,
    versionProvider = VersionProvider.class,
    hidden = true, // FIXME change after official release of StreamX Mesh Manager
    description = "Serves StreamX Mesh Manager locally.")
public class ManageCommand implements Runnable {

  private final Logger logger = Logger.getLogger(ManageCommand.class);

  public static final String COMMAND_NAME = "manage";
  public static final long CONTAINER_TIMEOUT_IN_SECS = 60_000L;

  @ArgGroup
  MeshSource meshSource;

  @Inject
  MeshResolver meshResolver;

  @Inject
  BannerPrinter bannerPrinter;

  @Inject
  ManageConfig manageConfig;

  @Inject
  DockerValidator dockerValidator;

  @Inject
  Event<MeshManagerStarted> meshManagerStartedEvent;

  @Override
  public void run() {
    dockerValidator.validateDockerEnvironment(Set.of(MeshManagerContainer.CONTAINER_NAME));

    var meshPath = meshResolver.resolveMeshPath(meshSource);
    var meshPathAsString = meshPath.toAbsolutePath().normalize().toString();


    var projectDirectory = meshPath.resolve("..");
    var projectDirectoryAsString = projectDirectory.toAbsolutePath().normalize().toString();
    logger.infov("Resolved mesh {0} and project directory {1}",
        meshPathAsString, projectDirectoryAsString);

    bannerPrinter.printBanner();

    print("Setting up StreamX Mesh Manager...");

    try (var meshManagerContainer = new MeshManagerContainer(
        manageConfig.meshManagerImage(),
        manageConfig.meshManagerPort(),
        meshPathAsString,
        projectDirectoryAsString
    )) {
      meshManagerContainer.start();

      print("StreamX Mesh Manager started on http://localhost:" + manageConfig.meshManagerPort());

      meshManagerStartedEvent.fire(new MeshManagerStarted());

      Quarkus.waitForExit();
    } catch (DockerContainerNonUniqueException e) {
      throw DockerException.nonUniqueContainersException(e.getContainers());
    } catch (DockerEnvironmentException e) {
      throw DockerException.dockerEnvironmentException();
    } catch (ContainerStartupTimeoutException e) {
      throw DockerException.containerStartupFailed(
          e.getContainerName(), CONTAINER_TIMEOUT_IN_SECS);
    } catch (Exception e) {
      throw throwMeshException(meshPath, e);
    }
  }

  private RuntimeException throwMeshException(Path meshPath, Exception e) {
    return new RuntimeException(
        ExceptionUtils.appendLogSuggestion(
            "Unable to read mesh definition from '" + meshPath + "'.\n"
                + "\n"
                + "Details:\n"
                + e.getMessage()), e);
  }
}
