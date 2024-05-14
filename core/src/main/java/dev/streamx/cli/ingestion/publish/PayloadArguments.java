package dev.streamx.cli.ingestion.publish;

import java.util.ArrayList;
import java.util.List;
import picocli.CommandLine.ArgGroup;

public class PayloadArguments {

  @ArgGroup(exclusive = false, multiplicity = "0..*",
      heading = """

          @|bold Payload defining arguments:|@
              Payload can be defined by specifying explicit full payload.
                e.g. @|yellow publish -j "{}" (...)|@
              will send @|yellow {}|@ to ingestion service.\s
              Note that @|yellow -j|@ is required to send json data
              (by default it would be sent as @|bold string|@)
    
              Payload can also be created by specifying (one or more)
              jsonPath with payload fragment
                e.g. @|yellow publish -s type=string -s content.bytes=hello (...)|@
              will send @|yellow {"type":"string","content":{"bytes":"hello"}}|@
              to ingestion service.
    
              There are few possibilities to define parameter value:
              * if value has prefix @|yellow file://|@ then value will be loaded
                from file with given (relative or absolute) path
              * otherwise raw value will be used as value
    
              Payload fragment type:
              * if value is @|bold string|@ fragment use @|yellow -s|@ option
              * if value is @|bold json|@ fragment use @|yellow -j|@ option
              * if value is @|bold binary|@ value fragment use @|yellow -b|@ option\s
          """
  )
  List<PayloadArgument> payloadArgs = new ArrayList<>();

  public List<PayloadArgument> getPayloadArgs() {
    return payloadArgs;
  }
}
