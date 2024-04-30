package dev.streamx.cli.ingestion.publish;

import java.util.Optional;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;

public class DataArguments {

  @ArgGroup(exclusive = true)
  DataType dataType;

  @Option(names = {"-d", "--data"},
      // FIXME
      description = "FIXME Pair of JsonPath and it's replacements. "
                    + "By default replacement is considered as json data.",
      required = true
  )
  String value;

  public static DataArguments of(String data) {
    DataArguments arg = new DataArguments();
    arg.value = data;

    return arg;
  }

  static class DataType {
    @Option(names = "-b",
        description = "Indicates that replacement is binary data",
        defaultValue = "false"
    )
    boolean binary;


    @Option(names = "-j",
        description = "Indicates that replacement is valid json",
        defaultValue = "false"
    )
    boolean json;

    public boolean isBinary() {
      return binary;
    }

    public boolean isJson() {
      return json;
    }
  }

  public String getValue() {
    return value;
  }

  public boolean isBinary() {
    return Optional.ofNullable(dataType).map(DataType::isBinary).orElse(false);
  }

  public boolean isJson() {
    return Optional.ofNullable(dataType).map(DataType::isJson).orElse(false);
  }
}
