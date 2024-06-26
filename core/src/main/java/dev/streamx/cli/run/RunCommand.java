package dev.streamx.cli.run;

import static dev.streamx.runner.main.Main.StreamxApp.printSummary;

import dev.streamx.cli.exception.DockerException;
import dev.streamx.cli.run.MeshDefinitionResolver.MeshDefinition;
import dev.streamx.runner.StreamxRunner;
import dev.streamx.runner.event.ContainerStarted;
import dev.streamx.runner.validation.excpetion.DockerContainerNonUniqueException;
import dev.streamx.runner.validation.excpetion.DockerEnvironmentException;
import io.quarkus.runtime.Quarkus;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import java.io.IOException;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "run", mixinStandardHelpOptions = true)
public class RunCommand implements Runnable {

  private static final String BANNER = """
       ____  _                           __  __
      / ___|| |_ _ __ ___  __ _ _ __ ___ \\ \\/ /
      \\___ \\| __| '__/ _ \\/ _` | '_ ` _ \\ \\  /\s
       ___) | |_| | |  __/ (_| | | | | | |/  \\\s
      |____/ \\__|_|  \\___|\\__,_|_| |_| |_/_/\\_\\.dev
                                               \s""";

  @ArgGroup
  MeshSource meshSource;

  static class MeshSource {

    @Option(names = {"-f", "--file"}, paramLabel = "mesh definition file",
        description = "Path to mesh definition file")
    String meshDefinitionFile;
  }

  @Inject
  StreamxRunner runner;

  @Inject
  MeshDefinitionResolver meshDefinitionResolver;

  @Override
  public void run() {
    try {
      print(BANNER);
      MeshDefinition result = meshDefinitionResolver.resolve(meshSource);
      String meshPath = result.path().normalize().toAbsolutePath().toString();

      print("Setting up system containers...");

      try {
        this.runner.initialize(result.serviceMesh(), meshPath);
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
    }
  }

  void onContainerStarted(@Observes ContainerStarted event) {
    print("- " + event.getContainerName() + " ready.");
  }

  private static void print(String x) {
    System.out.println(x);
  }
}
