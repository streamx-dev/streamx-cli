package dev.streamx.cli.command.run;

import dev.streamx.cli.BannerPrinter;
import dev.streamx.cli.VersionProvider;
import dev.streamx.cli.command.meshprocessing.MeshConfig;
import dev.streamx.cli.command.meshprocessing.MeshManager;
import dev.streamx.cli.command.meshprocessing.MeshResolver;
import dev.streamx.cli.command.meshprocessing.MeshSource;
import dev.streamx.cli.exception.DockerException;
import dev.streamx.runner.StreamxRunner;
import dev.streamx.runner.exception.ContainerStartupTimeoutException;
import io.quarkus.runtime.Quarkus;
import jakarta.inject.Inject;
import java.nio.file.Path;
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
  MeshConfig meshConfig;

  @Inject
  MeshResolver meshResolver;

  @Inject
  StreamxRunner runner;

  @Inject
  BannerPrinter bannerPrinter;

  @Inject
  MeshManager meshManager;

  @Override
  public void run() {
    try {
      Path meshPath = meshResolver.resolveMeshPath(meshConfig);
      meshManager.initializeMesh(meshPath);

      bannerPrinter.printBanner();
      meshManager.initializeRunMode(meshPath);

      meshManager.start();

      Quarkus.waitForExit();
    } catch (ContainerStartupTimeoutException e) {
      throw DockerException.containerStartupFailed(
          e.getContainerName(),
          runner.getContext().getStreamxBaseConfig().getContainerStartupTimeout());
    }
  }
}
