package dev.streamx.cli.run;

import static dev.streamx.runner.main.Main.StreamxApp.printSummary;

import dev.streamx.cli.run.MeshDefinitionResolver.MeshDefinition;
import dev.streamx.runner.StreamxRunner;
import dev.streamx.runner.event.ContainerStarted;
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

  private static final String LICENCE = """
      Distributed under StreamX End-User License Agreement 1.0
      https://www.streamx.dev/licenses/eula-v1-0.html
      """;

  @ArgGroup
  MeshSource meshSource;

  static class MeshSource {
    @Option(names = { "-f", "--file" }, paramLabel = "mesh definition file",
        description = "Path to mesh definition file")
    String meshDefinitionFile;

    @Option(names = { "--blueprints-mesh" },
        description = "Use predefined blueprints-mesh")
    boolean blueprintsMesh;
  }

  @Inject
  StreamxRunner runner;

  @Inject
  MeshDefinitionResolver meshDefinitionResolver;

  @Override
  public void run() {
    try {
      print(BANNER);
      print(LICENCE);
      MeshDefinition result = meshDefinitionResolver.resolve(meshSource);

      print("Setting up system containers...");

      this.runner.startBase(result.serviceMesh().getTenant());

      print("");
      print("Starting DX Mesh...");

      this.runner.startMesh(result.serviceMesh());

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
