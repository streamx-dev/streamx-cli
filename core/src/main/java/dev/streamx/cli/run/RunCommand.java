package dev.streamx.cli.run;

import static dev.streamx.runner.main.Main.StreamxApp.printSummary;

import dev.streamx.cli.VersionProvider;
import dev.streamx.cli.exception.DockerException;
import dev.streamx.cli.run.MeshDefinitionResolver.MeshDefinition;
import dev.streamx.runner.StreamxRunner;
import dev.streamx.runner.StreamxRunnerParams;
import dev.streamx.runner.event.ContainerStarted;
import dev.streamx.runner.validation.excpetion.DockerContainerNonUniqueException;
import dev.streamx.runner.validation.excpetion.DockerEnvironmentException;
import io.quarkus.runtime.Quarkus;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import java.io.IOException;
import org.testcontainers.containers.ContainerLaunchException;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = RunCommand.COMMAND_NAME,
    mixinStandardHelpOptions = true,
    versionProvider = VersionProvider.class,
    description = "Run a StreamX Mesh locally.")
public class RunCommand implements Runnable {
  public static final String COMMAND_NAME = "run";

  @ArgGroup
  MeshSource meshSource;

  static class MeshSource {

    @Option(names = {"-f", "--file"}, paramLabel = "mesh definition file",
        description = "Path to mesh definition file.")
    String meshDefinitionFile;
  }

  @Inject
  StreamxRunner runner;

  @Inject
  MeshDefinitionResolver meshDefinitionResolver;

  @Inject
  RunConfig runConfig;

  @Override
  public void run() {
    try {
      MeshDefinition result = meshDefinitionResolver.resolve(meshSource);
      String meshPath = result.path().normalize().toAbsolutePath().toString();

      print("Setting up system containers...");

      try {
        Long containerStartupTimeoutSeconds = runConfig.containerStartupTimeoutSeconds()
            .orElse(null);
        StreamxRunnerParams params = new StreamxRunnerParams(meshPath,
            containerStartupTimeoutSeconds);

        this.runner.initialize(result.serviceMesh(), params);
      } catch (DockerContainerNonUniqueException e) {
        throw DockerException.nonUniqueContainersException(e.getContainers());
      } catch (DockerEnvironmentException e) {
        throw DockerException.dockerEnvironmentException();
      }

      this.runner.startBase();

      print("");
      print("Starting DX Mesh...");

      this.runner.startMesh();

      printSummary(this.runner, result.path());
      Quarkus.waitForExit();
    } catch (IOException e) {
      throw new RuntimeException("Cannot run StreamX", e);
    } catch (ContainerLaunchException e) {
      throw DockerException.containerStartupFailed(e.getMessage());
    }
  }

  void onContainerStarted(@Observes ContainerStarted event) {
    print("- " + event.getContainerName() + " ready.");
  }

  private static void print(String x) {
    System.out.println(x);
  }
}
