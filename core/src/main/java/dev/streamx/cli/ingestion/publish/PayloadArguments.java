package dev.streamx.cli.ingestion.publish;

import java.util.ArrayList;
import java.util.List;
import picocli.CommandLine.ArgGroup;

public class PayloadArguments {

  @ArgGroup(exclusive = false, multiplicity = "0..*")
  List<DataArguments> dataArgs = new ArrayList<>();

  public List<DataArguments> getDataArgs() {
    return dataArgs;
  }
}
