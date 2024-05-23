package dev.streamx.cli.test.tools.terminal.command;

public class ShellCommandStrategy implements OsCommandStrategy {

  @Override
  public ProcessBuilder create(String command) {
    String[] commandArray = {"sh", "-c", command};
    return new ProcessBuilder(commandArray);
  }
}
