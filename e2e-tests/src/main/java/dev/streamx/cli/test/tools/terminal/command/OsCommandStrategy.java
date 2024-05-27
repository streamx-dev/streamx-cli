package dev.streamx.cli.test.tools.terminal.command;

public interface OsCommandStrategy {

  ProcessBuilder create(String command);
}
