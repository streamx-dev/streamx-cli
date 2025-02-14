package dev.streamx.cli.command.ingestion.stream;

import picocli.CommandLine.Parameters;

public class StreamIngestionArguments {

  @Parameters(index = "0", description = "Channel that message will be published to", arity = "1")
  String channel;
  @Parameters(index = "1", description = "Source file for the stream publication. "
      + "Can contain one or many messages", arity = "1")
  String sourceFile;


  public String getSourceFile() {
    return sourceFile;
  }

  public String getChannel() {
    return channel;
  }
}
