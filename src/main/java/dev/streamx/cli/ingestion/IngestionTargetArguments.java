package dev.streamx.cli.ingestion;

import picocli.CommandLine.Parameters;

public class IngestionTargetArguments {

  @Parameters(index = "0", description = "Channel that message will be published to", arity = "1")
  String channel;

  @Parameters(index = "1", description = "Message key", arity = "1")
  String key;

  public String getChannel() {
    return channel;
  }

  public String getKey() {
    return key;
  }
}

