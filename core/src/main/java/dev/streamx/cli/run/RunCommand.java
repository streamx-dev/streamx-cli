package dev.streamx.cli.run;

import static dev.streamx.cli.run.MeshDefinitionResolver.CURRENT_DIRECTORY_MESH;
import static dev.streamx.runner.main.Main.StreamxApp.printSummary;

import dev.streamx.cli.BannerPrinter;
import dev.streamx.cli.VersionProvider;
import dev.streamx.cli.exception.DockerException;
import dev.streamx.cli.run.MeshDefinitionResolver.MeshDefinition;
import dev.streamx.cli.util.ExceptionUtils;
import dev.streamx.runner.StreamxRunner;
import dev.streamx.runner.event.ContainerStarted;
import dev.streamx.runner.exception.ContainerStartupTimeoutException;
import dev.streamx.runner.validation.excpetion.DockerContainerNonUniqueException;
import dev.streamx.runner.validation.excpetion.DockerEnvironmentException;
import io.quarkus.runtime.Quarkus;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
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
        description = "Path to mesh definition file.",
        defaultValue = CURRENT_DIRECTORY_MESH)
    String meshDefinitionFile;
  }

  @Inject
  StreamxRunner runner;

  @Inject
  MeshDefinitionResolver meshDefinitionResolver;

  @Inject
  BannerPrinter bannerPrinter;

  @Override
  public void run() {
    try {
      MeshDefinitionResolvingResult meshDefinition = resolveMeshDefinition();

      bannerPrinter.printBanner();

      print("Setting up system containers...");

      try {
        this.runner.initialize(meshDefinition.result().serviceMesh(), meshDefinition.meshPath());
      } catch (DockerContainerNonUniqueException e) {
        throw DockerException.nonUniqueContainersException(e.getContainers());
      } catch (DockerEnvironmentException e) {
        throw DockerException.dockerEnvironmentException();
      } catch (Exception e) {
        throw throwMeshException(e);
      }

      this.runner.startBase();

      print("");
      print("Starting DX Mesh...");

      this.runner.startMesh();
      RunningMeshPropertiesGenerator.generateRootAuthToken(this.runner.getContext());

      printSummary(this.runner, meshDefinition.result().path());
      Quarkus.waitForExit();
    } catch (ContainerStartupTimeoutException e) {
      throw DockerException.containerStartupFailed(
          e.getContainerName(),
          runner.getContext().getStreamxBaseConfig().getContainerStartupTimeout());
    }
  }

  @NotNull
  private MeshDefinitionResolvingResult resolveMeshDefinition() {
    try {
      MeshDefinition result = meshDefinitionResolver.resolve(meshSource);
      String meshPath = result.path().normalize().toAbsolutePath().toString();

      return new MeshDefinitionResolvingResult(result, meshPath);
    } catch (Exception e) {
      throw throwMeshException(e);
    }
  }

  private RuntimeException throwMeshException(Exception e) {
    var path = Optional.ofNullable(meshSource)
        .map(meshSource -> meshSource.meshDefinitionFile)
        .orElse(CURRENT_DIRECTORY_MESH);

    return new RuntimeException(
        ExceptionUtils.appendLogSuggestion("Unable to read mesh definition from '" + path + "'.\n"
        + "\n"
        + "Details:\n"
        + e.getMessage()), e);
  }

  private record MeshDefinitionResolvingResult(MeshDefinition result, String meshPath) {

  }

  void onContainerStarted(@Observes ContainerStarted event) {
    print("- " + event.getContainerName() + " ready.");
  }

  private static void print(String x) {
    System.out.println(x);
  }
}
