package dev.streamx.cli.command.dev;

import static dev.streamx.cli.util.Output.print;

import dev.streamx.cli.BannerPrinter;
import dev.streamx.cli.VersionProvider;
import dev.streamx.cli.command.dev.event.DevReady;
import dev.streamx.cli.command.meshprocessing.MeshConfig;
import dev.streamx.cli.command.meshprocessing.MeshManager;
import dev.streamx.cli.command.meshprocessing.MeshResolver;
import dev.streamx.cli.command.meshprocessing.MeshSource;
import dev.streamx.cli.command.meshprocessing.MeshWatcher;
import dev.streamx.cli.exception.DockerException;
import dev.streamx.runner.StreamxRunner;
import dev.streamx.runner.container.PulsarContainer;
import dev.streamx.runner.exception.ContainerStartupTimeoutException;
import io.quarkus.runtime.Quarkus;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import org.jboss.logging.Logger;
import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Spec;

@Command(name = DevCommand.COMMAND_NAME,
    mixinStandardHelpOptions = true,
    versionProvider = VersionProvider.class,
    description = "Develop a StreamX Mesh locally.")
public class DevCommand implements Runnable {

  public static final String COMMAND_NAME = "dev";

  @Inject
  Logger logger;

  @ArgGroup
  MeshSource meshSource;

  @Spec
  CommandLine.Model.CommandSpec spec;

  @Inject
  MeshConfig meshConfig;

  @Inject
  MeshResolver meshResolver;

  @Inject
  DockerValidator dockerValidator;

  @Inject
  StreamxRunner runner;

  @Inject
  MeshWatcher meshWatcher;

  @Inject
  BannerPrinter bannerPrinter;

  @Inject
  MeshManager meshManager;

  @Inject
  DashboardRunner dashboardRunner;

  @Inject
  Event<DevReady> devReadyEvent;

  @Override
  public void run() {
    try {
      Path meshPath = meshResolver.resolveMeshPath(meshConfig, false);

      dockerValidator.validateDockerEnvironment(Set.of(
          DashboardContainer.CONTAINER_NAME,
          PulsarContainer.NAME
      ));

      bannerPrinter.printBanner();
      boolean meshFileExists = meshPath.toFile().exists();
      if (!meshFileExists) {
        Files.createFile(meshPath);
      }

      startDashboard(meshPath);

      meshManager.initializeDevMode(meshPath, spec.commandLine());
      meshWatcher.watchMeshChanges(meshPath);

      if (meshFileExists) {
        meshManager.start();
      }

      devReadyEvent.fire(new DevReady());

      Quarkus.waitForExit();
    } catch (ContainerStartupTimeoutException e) {
      throw DockerException.containerStartupFailed(
          e.getContainerName(),
          runner.getContext().getStreamxBaseConfig().getContainerStartupTimeout());
    } catch (IOException e) {
      // handle creat file
      throw new RuntimeException(e);
    }
  }

  private void startDashboard(Path meshPath) {
    print("Setting up StreamX Dashboard...");
    var meshPathAsString = meshPath.toAbsolutePath().normalize().toString();
    Path meshDirectory = meshPath.resolve("..");
    Path projectDirectory = null;
    if (Files.exists(meshDirectory.resolve("..").normalize())) {
      projectDirectory = meshDirectory.resolve("..").normalize();
    }

    var meshDirectoryAsString = meshDirectory.toAbsolutePath().normalize().toString();
    var projectDirectoryAsString = projectDirectory != null
        ? projectDirectory.toAbsolutePath().normalize().toString()
        : null;

    logger.infov("Resolved mesh {0}, mesh directory {1} and project directory {2}",
        meshPathAsString, meshDirectoryAsString, projectDirectoryAsString);
    dashboardRunner.startStreamxDashboard(meshPathAsString, meshDirectoryAsString,
        projectDirectoryAsString);
  }
}
