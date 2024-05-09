package dev.streamx.cli;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EntrypointMain {

  private static final SimpleDateFormat DATE_FORMAT =
      new SimpleDateFormat("yyyy_MM_dd__HH_mm_ss_SSS");
  private static final String LOG_FILE_PATH_PROPERTY_NAME = "%prod.quarkus.log.file.path";
  private static final String STREAMX_LOG_FILE_NAME_PATTERN = ".streamx/streamx-%s.log";

  public static void main(String[] args) {
    int javaVersion = getJavaVersion();

    if (javaVersion < 17) {
      System.out.println("Java 17 or higher is required!");
      return;
    }

    overrideLogFileName();
    runStreamxCommand(args);
  }

  private static int getJavaVersion() {
    String version = System.getProperty("java.version");
    if (version.startsWith("1.")) {
      version = version.substring(2, 3);
    } else {
      int dot = version.indexOf(".");
      if (dot != -1) {
        version = version.substring(0, dot);
      }
    }
    return Integer.parseInt(version);
  }

  private static void overrideLogFileName() {
    if (System.getProperty(LOG_FILE_PATH_PROPERTY_NAME) != null) {
      return;
    }

    String date = DATE_FORMAT.format(new Date());
    String streamxLogPath = String.format(STREAMX_LOG_FILE_NAME_PATTERN, date);

    System.setProperty(LOG_FILE_PATH_PROPERTY_NAME, streamxLogPath);
  }

  private static void runStreamxCommand(String[] args) {
    try {
      Class<?> streamxCommand = Class.forName("dev.streamx.cli.StreamxCommand");
      Method main = streamxCommand.getDeclaredMethod("main", String[].class);

      main.invoke(null, new Object[]{args});
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
