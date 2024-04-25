package dev.streamx.cli.ingestion.publish;

import java.util.Optional;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;

public class ValueArguments {
    @Option(names = {"-v", "--value"},
        description = "Pair of JsonPath and it's replacements. By default replacement is considered as json data.",
        required = true
    )
    String value;

    @ArgGroup(exclusive = true)
    ValueType valueType;

    static class ValueType {
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
      return Optional.ofNullable(valueType).map(ValueType::isBinary).orElse(false);
    }

    public boolean isString() {
      return Optional.ofNullable(valueType).map(ValueType::isString).orElse(false);
    }
  }
