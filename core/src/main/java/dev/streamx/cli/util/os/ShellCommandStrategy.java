package dev.streamx.cli.util.os;

public class ShellCommandStrategy implements OsCommandStrategy {

  @Override
  public ProcessBuilder create(String command) {
    String[] commandArray = {"sh", "-c", command};
    return new ProcessBuilder(commandArray);
  }
}
