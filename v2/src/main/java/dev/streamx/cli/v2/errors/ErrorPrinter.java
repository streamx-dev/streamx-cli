package dev.streamx.cli.v2.errors;

import org.jboss.logging.Logger;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ErrorPrinter {
  public static void print(Logger logger, Throwable e, boolean withStackTrace) {
    if (withStackTrace) {
      logger.error(getStackTraceAsString(e));
      return;
    }

    logger.error(e.getMessage());
  }

  private static String getStackTraceAsString(Throwable throwable) {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    throwable.printStackTrace(pw);
    return sw.toString();
  }
}
