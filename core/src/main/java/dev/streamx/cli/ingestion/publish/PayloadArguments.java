package dev.streamx.cli.ingestion.publish;

import java.util.ArrayList;
import java.util.List;
import picocli.CommandLine.ArgGroup;

public class PayloadArguments {

  @ArgGroup(exclusive = false, multiplicity = "0..*",
      heading = """

          @|bold,italic Payload Defining Options|@:
              Payload can be defined by specifying an explicit full payload.
              For example, @|yellow publish -j "{}" (...)|@
              will send @|yellow {}|@ to the ingestion service.
    
              A payload can also be created by specifying one or more
              JSONPath expressions with payload fragments.
              For example, @|yellow publish -s type=string -s content.bytes=hello (...)|@
              will send @|yellow {"type":"string","content":{"bytes":"hello"}}|@
              to the ingestion service.
    
              There are few ways to define the parameter value:
              * if value has prefix @|yellow file://|@ then value will be loaded
                from file with given (relative or absolute) path
              * otherwise raw value will be used as value
    
              Payload fragment type:
              * if the value is a @|bold string|@ fragment, use @|yellow -s|@ option
              * if the value is a @|bold JSON|@ fragment, use @|yellow -j|@ option
              * if the value is a @|bold binary|@ value fragment use @|yellow -b|@ option

          """
  )
  List<PayloadArgument> payloadArgs = new ArrayList<>();

  public List<PayloadArgument> getPayloadArgs() {
    return payloadArgs;
  }
}
