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
    // FIXME change to "-j" if it's json
    @Option(names = "-b",
        description = "Indicates that replacement is binary data",
        defaultValue = "false"
    )
    boolean binary;

    @Option(names = "-s",
        description = "Indicates that replacement is raw string",
        defaultValue = "false"
    )
    boolean string;

    public boolean isBinary() {
      return binary;
    }

    public boolean isString() {
      return string;
    }
  }

  public String getValue() {
    return value;
  }

  public boolean isBinary() {
    return Optional.ofNullable(dataType).map(DataType::isBinary).orElse(false);
  }

  public boolean isString() {
    return Optional.ofNullable(dataType).map(DataType::isString).orElse(false);
  }
}
