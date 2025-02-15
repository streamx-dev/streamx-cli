package dev.streamx.cli.command.ingestion.batch;

import picocli.CommandLine.Parameters;

public class BatchIngestionArguments {

  public enum ActionType {
    publish, unpublish;
  }

  @Parameters(index = "0",
      description = "Action to perform, either ${COMPLETION-CANDIDATES}", arity = "1")
  ActionType action;

  @Parameters(index = "1", description = "Source directory for the batch publication", arity = "1")
  String sourceDirectory;

  public String getSourceDirectory() {
    return sourceDirectory;
  }

  public ActionType getAction() {
    return action;
  }
}
