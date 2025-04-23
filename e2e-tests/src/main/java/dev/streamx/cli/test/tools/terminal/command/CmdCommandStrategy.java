package dev.streamx.cli.test.tools.terminal.command;


public class CmdCommandStrategy implements OsCommandStrategy {

  @Override
  public ProcessBuilder create(String command) {
    String[] commandArray = {"cmd.exe", "/c", command};
    return new ProcessBuilder(commandArray);
  }
}
