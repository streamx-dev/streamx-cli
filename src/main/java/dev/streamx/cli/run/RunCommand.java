package dev.streamx.cli.run;

import static dev.streamx.runner.main.Main.StreamxApp.printSummary;

import dev.streamx.cli.run.RunConfigResolver.ConfigResolvingResult;
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
    @Option(names = { "-f", "--file" }, paramLabel = "config file",
        description = "Path to config file with mesh definition")
    String configFile;

    @Option(names = { "--blueprints-mesh" },
        description = "Use predefined blueprints-mesh")
    boolean blueprintsMesh;
  }

  @Inject
  StreamxRunner runner;

  @Inject
  RunConfigResolver configResolver;

  @Override
  public void run() {
    try {
      ConfigResolvingResult result = configResolver.resolveConfig(meshSource, spec);

      print("Starting Streamx...");

      this.runner.startBase(result.serviceMesh().getTenant());

      print("Streamx ready.");
      print("Starting mesh...");

      this.runner.startMesh(result.serviceMesh());

      printSummary(this.runner, result.path());
      Quarkus.waitForExit();
    } catch (IOException e) {
      throw new RuntimeException("Cannot run StreamX", e);
    }
  }

  void onMeshStarted(@Observes ContainerStarted event) {
    print("Container " + event.getContainerName() + " started.");
  }

  private static void print(String x) {
    System.out.println(x);
  }
}
