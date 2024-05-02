package dev.streamx.cli.ingestion.publish;

import dev.streamx.cli.ingestion.IngestionTargetArguments;
import picocli.CommandLine.Parameters;

public class PublishTargetArguments extends IngestionTargetArguments {

  @Parameters(index = "2", arity = "0..1",
      paramLabel = "payloadFile",
      description = "File containing payload to publish.\n"
          + "This is optional argument.\n"
          + "This argument shortcut to equal to \n"
          + "@|yellow -d=file://<payloadFile>|@ "
          + "specified before other @|yellow -d |@ params\n\n"
          + "If this argument occurs, it has highest priority in defining payload.")
  String payloadFile;

  public String getPayloadFile() {
    return payloadFile;
  }
}

