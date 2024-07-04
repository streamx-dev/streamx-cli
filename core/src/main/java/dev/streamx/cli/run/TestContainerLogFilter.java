package dev.streamx.cli.run;


import io.quarkus.logging.LoggingFilter;
import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.stream.Stream;

@LoggingFilter(name = "tc-pull-filter")
public class TestContainerLogFilter implements Filter {

  @Override
  public boolean isLoggable(LogRecord record) {
    Level level = record.getLevel();

    return isImagePullLog(record, level);
  }

  private static boolean isImagePullLog(LogRecord record, Level level) {
    return level.intValue() >= Level.WARNING.intValue() || Stream.of(
            "Pulling image",
            "Pull complete.",
            "Pulling docker image",
            "Starting to pull image"
        )
        .anyMatch(start -> record.getMessage().startsWith(start));
  }
}
