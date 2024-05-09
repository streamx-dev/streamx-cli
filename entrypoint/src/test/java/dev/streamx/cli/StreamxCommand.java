package dev.streamx.cli;

import java.util.concurrent.atomic.AtomicBoolean;

public class StreamxCommand {

  private static final AtomicBoolean LAUNCHED = new AtomicBoolean(false);

  public static void main(String[] args) {
    LAUNCHED.set(true);
  }

  public static boolean isLaunched() {
    return LAUNCHED.get();
  }

  public static void clearLaunched() {
    LAUNCHED.set(false);
  }
}