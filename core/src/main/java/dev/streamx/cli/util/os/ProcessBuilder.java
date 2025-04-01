package dev.streamx.cli.util.os;

import java.io.File;
import java.io.IOException;

public class ProcessBuilder {

  private final java.lang.ProcessBuilder processBuilder;

  public ProcessBuilder(String[] commandArray) {
    this.processBuilder = new java.lang.ProcessBuilder(commandArray);
  }

  public ProcessBuilder addEnv(String key, String value) {
    processBuilder.environment().put(key, value);
    return this;
  }

  public Process start() throws IOException {
    return processBuilder.start();
  }

  public ProcessBuilder directory(File file) {
    processBuilder.directory(file);
    return this;
  }
}
