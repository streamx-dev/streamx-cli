package dev.streamx.cli.command.ingestion.batch;

import picocli.CommandLine.Parameters;

public class BatchIngestionArguments {

  @Parameters(index = "0", description = "Source directory for the batch publication", arity = "1")
  String sourceDirectory;

  public String getSourceDirectory() {
    return sourceDirectory;
  }
}
