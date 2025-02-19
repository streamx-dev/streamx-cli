package dev.streamx.cli.util.os;

public interface OsCommandStrategy {

  ProcessBuilder create(String command);
}
