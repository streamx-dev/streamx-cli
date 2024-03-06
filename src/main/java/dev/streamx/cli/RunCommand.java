package dev.streamx.cli;

import static dev.streamx.runner.main.Main.StreamxApp.printSummary;

import dev.streamx.runner.StreamxRunner;
import dev.streamx.runner.main.Main;
import dev.streamx.runner.mapper.MeshConfigMapper;
import dev.streamx.runner.model.ServiceMesh;
import io.quarkus.runtime.Quarkus;
import jakarta.inject.Inject;
import java.io.IOException;
import java.nio.file.Path;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "run", mixinStandardHelpOptions = true)
public class RunCommand implements Runnable {

  private final MeshConfigMapper mapper = new MeshConfigMapper();

  @Option(names = { "-f", "--file" }, paramLabel = "config file",
      description = "Path to config file with mesh definition")
  String config;

  @Inject
  StreamxRunner runner;

  @Override
  public void run() {
    try {
      Path path = null;
      ServiceMesh serviceMesh;
      if (config != null) {
        path = Path.of(config);
        serviceMesh = this.mapper.read(path);
      } else {
        serviceMesh = mapper.read(Main.DEFAULT_MESH_YAML);
      }

      this.runner.startBase(serviceMesh.getTenant());
      this.runner.startMesh(serviceMesh);
      printSummary(this.runner, path);
      Quarkus.waitForExit();
    } catch (IOException e) {
      throw new RuntimeException("Cannot run StreamX", e);
    }
  }

}
