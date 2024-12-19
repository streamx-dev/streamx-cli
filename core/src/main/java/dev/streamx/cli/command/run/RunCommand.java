package dev.streamx.cli.command.run;

import static dev.streamx.cli.util.Output.print;
import static dev.streamx.runner.main.Main.StreamxApp.printSummary;

import dev.streamx.cli.BannerPrinter;
import dev.streamx.cli.VersionProvider;
import dev.streamx.cli.command.meshprocessing.MeshDefinitionResolver;
import dev.streamx.cli.command.meshprocessing.MeshResolver;
import dev.streamx.cli.command.meshprocessing.MeshSource;
import dev.streamx.cli.exception.DockerException;
import dev.streamx.cli.util.ExceptionUtils;
import dev.streamx.mesh.model.ServiceMesh;
import dev.streamx.runner.StreamxRunner;
import dev.streamx.runner.event.ContainerStarted;
import dev.streamx.runner.exception.ContainerStartupTimeoutException;
import dev.streamx.runner.validation.excpetion.DockerContainerNonUniqueException;
import dev.streamx.runner.validation.excpetion.DockerEnvironmentException;
import io.quarkus.runtime.Quarkus;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;

@Command(name = RunCommand.COMMAND_NAME,
    mixinStandardHelpOptions = true,
    versionProvider = VersionProvider.class,
    description = "Run a StreamX Mesh locally.")
public class RunCommand implements Runnable {

  public static final String COMMAND_NAME = "run";

  @ArgGroup
  MeshSource meshSource;

  @Inject
  StreamxRunner runner;

  @Inject
  MeshResolver meshResolver;

  @Inject
  MeshDefinitionResolver meshDefinitionResolver;

  @Inject
  BannerPrinter bannerPrinter;

  @Override
  public void run() {
    try {
      var meshPath = meshResolver.resolveMeshPath(meshSource);
      var normalizedMeshPath = meshPath.normalize().toAbsolutePath();
      var meshPathAsString = normalizedMeshPath.toString();

      var serviceMesh = resolveMeshDefinition(meshPath);

      bannerPrinter.printBanner();

      print("Setting up system containers...");

      try {
        this.runner.initialize(serviceMesh, meshPathAsString);
      } catch (DockerContainerNonUniqueException e) {
        throw DockerException.nonUniqueContainersException(e.getContainers());
      } catch (DockerEnvironmentException e) {
        throw DockerException.dockerEnvironmentException();
      } catch (Exception e) {
        throw throwMeshException(meshPath, e);
      }

      this.runner.startBase();

      print("");
      print("Starting DX Mesh...");

      this.runner.startMesh();
      RunningMeshPropertiesGenerator.generateRootAuthToken(this.runner.getMeshContext());

      printSummary(this.runner, normalizedMeshPath);
      Quarkus.waitForExit();
    } catch (ContainerStartupTimeoutException e) {
      throw DockerException.containerStartupFailed(
          e.getContainerName(),
          runner.getContext().getStreamxBaseConfig().getContainerStartupTimeout());
    }
  }

  @NotNull
  private ServiceMesh resolveMeshDefinition(Path meshPath) {
    try {
      return meshDefinitionResolver.resolve(meshPath);
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

  void onContainerStarted(@Observes ContainerStarted event) {
    print("- " + event.getContainerName() + " ready.");
  }
}
