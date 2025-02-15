package dev.streamx.cli.command.ingestion.publish;

import dev.streamx.cli.command.ingestion.IngestionTargetArguments;
import picocli.CommandLine.Parameters;

public class PublishArguments extends IngestionTargetArguments {

  @Parameters(index = "2", arity = "0..1",
      paramLabel = "payloadFile",
      description = """
          File containing the payload to be published.
          This is an optional parameter.
          This parameter is equivalent to\s
          @|yellow -j=file://<payloadFile>|@ specified before other \
          @|bold,italic Payload Defining Option|@.

          If this parameter is present, it has the highest priority in defining the payload.""")
  String payloadFile;

  public String getPayloadFile() {
    return payloadFile;
  }
}

