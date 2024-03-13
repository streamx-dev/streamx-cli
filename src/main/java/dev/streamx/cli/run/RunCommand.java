package dev.streamx.cli.run;

import static dev.streamx.runner.main.Main.StreamxApp.printSummary;

import dev.streamx.cli.run.MeshDefinitionResolver.MeshDefinition;
import dev.streamx.runner.StreamxRunner;
import dev.streamx.runner.event.ContainerStarted;
import io.quarkus.runtime.Quarkus;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import java.io.IOException;
import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

@Command(name = "run", mixinStandardHelpOptions = true)
public class RunCommand implements Runnable {

  @Spec
  CommandLine.Model.CommandSpec spec;

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
      MeshDefinition result = meshDefinitionResolver.resolve(meshSource);

      print("1. Observability and event streaming");

      this.runner.startBase(result.serviceMesh().getTenant());

      print("2. Observability and event streaming [OK]");
      print("3. Starting Mesh");

      this.runner.startMesh(result.serviceMesh());

      printSummary(this.runner, result.path());
      Quarkus.waitForExit();
    } catch (IOException e) {
      throw new RuntimeException("Cannot run StreamX", e);
    }
  }

  void onContainerStarted(@Observes ContainerStarted event) {
    print("   - " + event.getContainerName() + " ready.");
  }

  private static void print(String x) {
    System.out.println(x);
  }
}
