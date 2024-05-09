package dev.streamx.cli;

import java.lang.reflect.Method;

public class EntrypointMain {

  public static void main(String[] args) {
    int javaVersion = getJavaVersion();

    if (javaVersion >= 17) {
      try {
        Class<?> streamxCommand = Class.forName("dev.streamx.cli.StreamxCommand");
        Method main = streamxCommand.getDeclaredMethod("main", String[].class);
        main.invoke(null, new Object[]{args});
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    } else {
      System.out.println("Java 17 or higher is required!");
    }
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
}
