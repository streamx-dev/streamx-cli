package dev.streamx.cli.ingestion.publish;

import java.util.ArrayList;
import java.util.List;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;

public class PayloadArguments {
  @Option(names = {"-d", "--data"},
      description = "Published payload",
      defaultValue = "{}")
  String data;

  @ArgGroup(exclusive = false, multiplicity = "0..*")
  List<ValueArguments> values = new ArrayList<>();
}
