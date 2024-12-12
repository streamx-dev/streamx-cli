package dev.streamx.cli.util;

import org.jboss.logging.Logger;

public class Output {

  private static final Logger logger = Logger.getLogger(Output.class);

  public static void print(String x) {
    System.out.println(x);
    logger.info(x);
  }

  public static void printf(String format, Object ... args) {
    System.out.printf(format, args);
    logger.infof(format, args);
  }
}
