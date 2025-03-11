package dev.streamx.cli.command.dev;

import static dev.streamx.cli.util.Output.print;

import dev.streamx.cli.BannerPrinter;
import dev.streamx.cli.VersionProvider;
import dev.streamx.cli.command.dev.event.DashboardStarted;
import dev.streamx.cli.command.meshprocessing.MeshConfig;
import dev.streamx.cli.command.meshprocessing.MeshManager;
import dev.streamx.cli.command.meshprocessing.MeshResolver;
import dev.streamx.cli.command.meshprocessing.MeshSource;
import dev.streamx.cli.command.meshprocessing.MeshWatcher;
import dev.streamx.cli.exception.DockerException;
import dev.streamx.cli.util.StreamxMavenPropertiesUtils;
import dev.streamx.runner.StreamxRunner;
import dev.streamx.runner.container.PulsarContainer;
import dev.streamx.runner.exception.ContainerStartupTimeoutException;
import io.quarkus.runtime.Quarkus;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
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

  private static final long CONTAINER_TIMEOUT_IN_SECS = 60_000L;
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
  DevConfig devConfig;

  @Inject
  Event<DashboardStarted> dashboardStartedEvent;

  @Override
  public void run() {
    try {
      Path meshPath = meshResolver.resolveMeshPath(meshConfig, false);

      dockerValidator.validateDockerEnvironment(Set.of(
          DashboardContainer.CONTAINER_NAME,
          PulsarContainer.NAME
      ));

      bannerPrinter.printBanner();
      meshManager.initializeDevMode(meshPath, spec.commandLine());
      if (meshPath.toFile().exists()) {
        meshManager.start();
      } else {
        Files.createFile(meshPath);
      }

      print("Setting up StreamX Dashboards...");

      var meshPathAsString = meshPath.toAbsolutePath().normalize().toString();
      var projectDirectory = meshPath.resolve("..");
      var projectDirectoryAsString = projectDirectory.toAbsolutePath().normalize().toString();

      logger.infov("Resolved mesh {0} and project directory {1}",
          meshPathAsString, projectDirectoryAsString);
      startStreamxDashboard(meshPathAsString, projectDirectoryAsString);

      meshWatcher.watchMeshChanges(meshPath);

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

  private void startStreamxDashboard(String meshPathAsString, String projectDirectoryAsString) {
    var dashboardContainer = new DashboardContainer(
        StreamxMavenPropertiesUtils.getDashboardImage(),
        devConfig.dashboardPort(),
        meshPathAsString,
        projectDirectoryAsString
    ).withStartupTimeout(Duration.ofSeconds(CONTAINER_TIMEOUT_IN_SECS));
    dashboardContainer.start();

    print("StreamX Dashboards started on http://localhost:" + devConfig.dashboardPort());

    dashboardStartedEvent.fire(new DashboardStarted());
  }
}
