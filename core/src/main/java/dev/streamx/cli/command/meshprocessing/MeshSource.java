package dev.streamx.cli.command.meshprocessing;

import dev.streamx.cli.config.ArgumentConfigSource;
import picocli.CommandLine.Option;

public class MeshSource {

  @Option(names = {"-f", "--file"}, paramLabel = "<meshDefinitionFile>",
      description = "Path to mesh definition file.")
  void meshDefinitionFile(String meshDefinitionFile) {
    ArgumentConfigSource.registerValue(MeshConfig.STREAMX_MESH_PATH, meshDefinitionFile);
  }
}

