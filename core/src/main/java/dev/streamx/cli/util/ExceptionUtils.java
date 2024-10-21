package dev.streamx.cli.util;

import org.eclipse.microprofile.config.ConfigProvider;

public final class ExceptionUtils {

  private ExceptionUtils() {

  }

  public static RuntimeException sneakyThrow(Throwable t) {
    if (t == null) {
      throw new NullPointerException("t");
    }
    return ExceptionUtils.<RuntimeException>sneakyThrow0(t);
  }

  @SuppressWarnings("unchecked")
  private static <T extends Throwable> T sneakyThrow0(Throwable t) throws T {
    throw (T) t;
  }

  public static String appendLogSuggestion(String originalMessage) {
    String logPath = ConfigProvider.getConfig()
        .getValue("quarkus.log.file.path", String.class);

    return """
        %s
        
        Full logs can be found in %s"""
        .formatted(originalMessage, logPath);
  }
}
