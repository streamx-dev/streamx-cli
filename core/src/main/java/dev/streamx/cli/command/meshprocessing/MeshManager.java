package dev.streamx.cli.command.meshprocessing;

import static dev.streamx.cli.util.Output.print;
import static dev.streamx.runner.main.Main.StreamxApp.printSummary;

import dev.streamx.cli.ExecutionExceptionHandler;
import dev.streamx.cli.command.run.RunningMeshPropertiesGenerator;
import dev.streamx.cli.exception.DockerException;
import dev.streamx.cli.util.ExceptionUtils;
import dev.streamx.mesh.model.ServiceMesh;
import dev.streamx.runner.MeshContext;
import dev.streamx.runner.StreamxRunner;
import dev.streamx.runner.validation.excpetion.DockerContainerNonUniqueException;
import dev.streamx.runner.validation.excpetion.DockerEnvironmentException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import org.jetbrains.annotations.NotNull;
import org.testcontainers.containers.GenericContainer;
import picocli.CommandLine;

@ApplicationScoped
public class MeshManager {

  @Inject
  StreamxRunner runner;

  @Inject
  MeshDefinitionResolver meshDefinitionResolver;

  @Inject
  ExecutionExceptionHandler executionExceptionHandler;

  private boolean exceptionHandlingEnabled = false;
  private Path meshPath;
  private String meshPathAsString;
  private Path normalizedMeshPath;
  private CommandLine commandLine;
  private ServiceMesh serviceMesh;

  public void initializeMesh(Path meshPath) {
    this.meshPath = meshPath;
    this.normalizedMeshPath = meshPath.normalize().toAbsolutePath();
    this.meshPathAsString = normalizedMeshPath.toString();

    this.serviceMesh = resolveMeshDefinition(meshPath);;
  }

  public void initializeRunMode(Path meshPath) {
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
  }

  public void initializeDevMode(Path meshPath, CommandLine commandLine) {
    this.meshPath = meshPath;
    this.exceptionHandlingEnabled = true;
    this.commandLine = commandLine;

    normalizedMeshPath = meshPath.toAbsolutePath().normalize();
    meshPathAsString = normalizedMeshPath.toString();

    print("Setting up system containers...");

    this.runner.initializeBase();
    this.runner.startBase();
  }

  public void start() {
    execute(this::doStart);
  }

  private void doStart() {
    this.serviceMesh = resolveMeshDefinition(meshPath);

    try {
      this.runner.initialize(serviceMesh, meshPathAsString);
    } catch (DockerContainerNonUniqueException e) {
      throw DockerException.nonUniqueContainersException(e.getContainers());
    } catch (DockerEnvironmentException e) {
      throw DockerException.dockerEnvironmentException();
    } catch (Exception e) {
      throw throwMeshException(meshPath, e);
    }

    print("");
    print("Starting DX Mesh...");

    this.runner.startMesh();
    RunningMeshPropertiesGenerator.generateRootAuthToken(this.runner.getMeshContext());
    printSummary(this.runner, normalizedMeshPath);
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

  public void stop() {
    try {
      MeshContext meshContext = this.runner.getMeshContext();
      print("Stopping DX Mesh...");
      meshContext
          .getAllContainers()
          .parallelStream()
          .forEach(GenericContainer::stop);

      print("DX Mesh stopped...");
      print("");
    } catch (Exception e) {
      if (!exceptionHandlingEnabled) {
        throw ExceptionUtils.sneakyThrow(e);
      }
    }
  }

  public void reload() {
    Boolean meshValid = execute(() -> {
      var serviceMesh = resolveMeshDefinition(meshPath);
      serviceMesh.validate().assertValid();

      return true;
    });

    if (meshValid == null || !meshValid) {
      print("Mesh definition is invalid. Reloading cancelled...");
      return;
    }

    execute(this::stop);

    start();
  }

  private <T> T execute(Callable<T> callable) {
    try {
      return callable.call();
    } catch (Exception e) {
      if (exceptionHandlingEnabled) {
        executionExceptionHandler.handleExecutionException(e, commandLine);

        return null;
      } else {
        throw ExceptionUtils.sneakyThrow(e);
      }
    }
  }

  private void execute(Runnable runnable) {
    try {
      runnable.run();
    } catch (Exception e) {
      if (exceptionHandlingEnabled) {
        executionExceptionHandler.handleExecutionException(e, commandLine);
      } else {
        throw ExceptionUtils.sneakyThrow(e);
      }
    }
  }
}
