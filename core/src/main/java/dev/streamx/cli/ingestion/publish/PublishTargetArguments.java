package dev.streamx.cli.ingestion.publish;

import dev.streamx.cli.ingestion.IngestionTargetArguments;
import picocli.CommandLine.Parameters;

public class PublishTargetArguments extends IngestionTargetArguments {

  @Parameters(index = "2", description = "Publish payload", arity = "0..1")
  String payload;

  public String getPayload() {
    return payload;
  }
}

