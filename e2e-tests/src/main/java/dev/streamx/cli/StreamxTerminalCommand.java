package dev.streamx.cli;

public class StreamxTerminalCommand {

  private final String command;

  public StreamxTerminalCommand(String command) {
    this.command = command;
  }

  public String getCommand() {
    return command;
  }
}
