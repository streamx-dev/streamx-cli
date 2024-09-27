package dev.streamx.cli.run;

import static dev.streamx.cli.ingestion.IngestionClientConfig.STREAMX_INGESTION_ROOT_AUTH_TOKEN;
import static dev.streamx.runner.main.Main.StreamxApp.printSummary;

import dev.streamx.cli.BannerPrinter;
import dev.streamx.cli.VersionProvider;
import dev.streamx.cli.config.DotStreamxConfigSource;
import dev.streamx.cli.exception.DockerException;
import dev.streamx.cli.run.MeshDefinitionResolver.MeshDefinition;
import dev.streamx.runner.StreamxRunner;
import dev.streamx.runner.event.ContainerStarted;
import dev.streamx.runner.exception.ContainerStartupTimeoutException;
import dev.streamx.runner.validation.excpetion.DockerContainerNonUniqueException;
import dev.streamx.runner.validation.excpetion.DockerEnvironmentException;
import io.quarkus.runtime.Quarkus;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.io.IOUtils;
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
        description = "Path to mesh definition file.")
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
      }

      this.runner.startBase();

      print("");
      print("Starting DX Mesh...");

      this.runner.startMesh();
      setupRootAuthToken();

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
    } catch (IOException e) {
      var path = meshSource.meshDefinitionFile;

      throw new RuntimeException("Unable to read mesh definition from '" + path + "'.\n"
          + "\n"
          + "Details:\n"
          + e.getMessage(), e);
    }
  }

  private record MeshDefinitionResolvingResult(MeshDefinition result, String meshPath) {

  }

  void onContainerStarted(@Observes ContainerStarted event) {
    print("- " + event.getContainerName() + " ready.");
  }

  private static void print(String x) {
    System.out.println(x);
  }


  private void setupRootAuthToken() {
    Map<String, String> tokensBySource = this.runner.getContext().getTokensBySource();
    if (tokensBySource != null) {
      InputStream input = null;
      OutputStream output = null;
      try {
        String streamxConfigPath = DotStreamxConfigSource.getUrl().getPath();
        input = new FileInputStream(streamxConfigPath);

        Properties properties = new Properties();
        properties.load(input);
        properties.setProperty(STREAMX_INGESTION_ROOT_AUTH_TOKEN, tokensBySource.get("root"));

        output = new FileOutputStream(streamxConfigPath);
        properties.store(output, null);

      } catch (IOException e) {
        throw new RuntimeException("Failed to setup root authentication token", e);
      } finally {
        IOUtils.closeQuietly(input);
        IOUtils.closeQuietly(output);
      }
    }
  }
}
