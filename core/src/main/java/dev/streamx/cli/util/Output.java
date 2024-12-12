package dev.streamx.cli.util;

import io.quarkus.logging.Log;

public class Output {

  public static void print(String x) {
    System.out.println(x);
    Log.info(x);
  }

  public static void printf(String format, Object ... args) {
    System.out.printf(format, args);
    Log.infof(format, args);
  }
}
