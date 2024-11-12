package dev.streamx.cli.util;

public class Output {

  public static void print(String x) {
    System.out.println(x);
  }

  public static void printf(String format, Object ... args) {
    System.out.printf(format, args);
  }
}
