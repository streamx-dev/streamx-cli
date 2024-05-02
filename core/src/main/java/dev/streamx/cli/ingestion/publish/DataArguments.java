package dev.streamx.cli.ingestion.publish;

import java.util.Optional;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;

public class DataArguments {

  @ArgGroup(exclusive = true)
  DataType dataType;

  @Option(names = {"-d", "--data"},
      description =
          "Defines payload to publish. Payload can be defined by specifying explicit full "
          + "payload."
          + "\ne.g. @|yellow publish -jd \"{}\" (...)|@\n "
              + "will send @|yellow {}|@ to ingestion service. "
          + "\nNote that @|yellow -jd|@ is required to send json data "
              + "(by default it would be sent as @|bold string|@)\n"
          + "\n"
          + "Payload can also be created by specifying (one or more) jsonPath with payload fragment"
          + "\ne.g. @|yellow publish -d type=string -d content.bytes=hello (...)|@\n will send "
          + "@|yellow {\"content\":\"string\",\"content\":{\"bytes\":\"hello\"}}|@ "
              + "to ingestion service.\n"
          + "\n"
          + "There are few possibilities to define parameter value:\n"
          + "* if value has prefix @|yellow file://|@ then value will be loaded from file with "
          + "given (relative or absolute) path\n"
          + "* otherwise raw value will be used as value\n"
          + "\n"
          + "Payload fragment type:\n"
          + "* default type is @|bold string|@.\n"
          + "* if value is @|bold json|@ fragment use @|yellow -j|@\n"
          + "* if value is @|bold binary|@ value fragment use @|yellow -b|@ \n",
      required = true
  )
  String value;

  public static DataArguments of(String data) {
    DataArguments arg = new DataArguments();
    arg.value = data;

    return arg;
  }

  public static DataArguments ofJsonNode(String data) {
    DataArguments arg = of(data);
    arg.dataType = new DataType();
    arg.dataType.json = true;

    return arg;
  }

  public static DataArguments ofBinary(String data) {
    DataArguments arg = of(data);
    arg.dataType = new DataType();
    arg.dataType.binary = true;

    return arg;
  }

  static class DataType {
    @Option(names = "-b",
        description = "Indicates that payload fragment is binary data (see @|yellow -d|@)",
        defaultValue = "false"
    )
    boolean binary;


    @Option(names = "-j",
        description = "Indicates that payload fragment is valid json (see @|yellow -d|@)",
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
