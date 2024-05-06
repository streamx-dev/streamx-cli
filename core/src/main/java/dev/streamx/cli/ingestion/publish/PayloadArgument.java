package dev.streamx.cli.ingestion.publish;

import java.util.Optional;
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
    @Option(names = {"-s", "--string-content"},
        description = "Defines payload fragment of string type"
    )
    String string;

    @Option(names = { "-b", "--binary-content"},
        description = "Defines payload fragment of binary type"
    )
    String binary;

    @Option(names = { "-j", "--json-content"},
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
    throw new IllegalArgumentException("Unknown data parameter."); // FIXME
  }

  public boolean isBinary() {
    return Optional.ofNullable(payload).map(Payload::isBinary).orElse(false);
  }

  public boolean isJson() {
    return Optional.ofNullable(payload).map(Payload::isJson).orElse(false);
  }

  public boolean isString() {
    return Optional.ofNullable(payload).map(Payload::isString).orElse(false);
  }
}
