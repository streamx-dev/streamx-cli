package dev.streamx.cli.ingestion.publish;

import dev.streamx.cli.ingestion.publish.payload.typed.SourceType;
import org.apache.commons.lang3.StringUtils;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;

public class PayloadArgument {

  @ArgGroup(exclusive = true)
  Payload payload;

  public static PayloadArgument ofString(String data) {
    PayloadArgument arg = new PayloadArgument();
    arg.payload = new Payload();
    arg.payload.string = data;

    return arg;
  }

  public static PayloadArgument ofJsonNode(String data) {
    PayloadArgument arg = new PayloadArgument();
    arg.payload = new Payload();
    arg.payload.json = data;

    return arg;
  }

  public static PayloadArgument ofBinary(String data) {
    PayloadArgument arg = new PayloadArgument();
    arg.payload = new Payload();
    arg.payload.binary = data;

    return arg;
  }

  static class Payload {
    @Option(names = {"-s", "--string-fragment"},
        description = "Defines payload fragment of string type"
    )
    String string;

    @Option(names = { "-b", "--binary-fragment"},
        description = "Defines payload fragment of binary type"
    )
    String binary;

    @Option(names = { "-j", "--json-fragment"},
        description = "Defines payload fragment of json node type"
    )
    String json;

    public boolean isBinary() {
      return StringUtils.isNotBlank(binary);
    }

    public boolean isJson() {
      return StringUtils.isNotBlank(json);
    }

    public boolean isString() {
      return StringUtils.isNotBlank(string);
    }
  }

  public String getValue() {
    if (payload.isString()) {
      return payload.string;
    }
    if (payload.isBinary()) {
      return payload.binary;
    }
    if (payload.isJson()) {
      return payload.json;
    }
    throw new IllegalStateException("There must be some type. Something went wrong...");
  }

  public SourceType getSourceType() {
    if (payload != null) {
      if (payload.isBinary()) {
        return SourceType.BINARY;
      } else if (payload.isJson()) {
        return SourceType.JSON;
      }
    }

    return SourceType.STRING;
  }
}
