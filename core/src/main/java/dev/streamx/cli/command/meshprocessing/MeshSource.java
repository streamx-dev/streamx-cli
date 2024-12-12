package dev.streamx.cli.command.meshprocessing;

import picocli.CommandLine.Option;

public class MeshSource {

  @Option(names = {"-f", "--file"}, paramLabel = "<meshDefinitionFile>",
      description = "Path to mesh definition file.")
  String meshDefinitionFile;
}

