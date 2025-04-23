package dev.streamx.cli.util.os;


public class CmdCommandStrategy implements OsCommandStrategy {

  @Override
  public ProcessBuilder create(String command) {
    String[] commandArray = {"cmd.exe", "/c", command};
    return new ProcessBuilder(commandArray);
  }
}
