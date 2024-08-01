package dev.streamx.cli.run;


import io.quarkus.logging.LoggingFilter;
import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.stream.Stream;
import org.testcontainers.containers.ContainerLaunchException;

@LoggingFilter(name = "tc-pull-filter")
public class TestContainerLogFilter implements Filter {

  private boolean skipLogsFromFailedContainer = false;

  @Override
  public boolean isLoggable(LogRecord record) {
    Level level = record.getLevel();

    if (skipLogsFromFailedContainer(record)) {
      return false;
    }

    if (excludedExceptions(record)) {
      skipLogsFromFailedContainer = true;
      return false;
    }
    return isImagePullLog(record, level);
  }

  private boolean skipLogsFromFailedContainer(LogRecord record) {
    return skipLogsFromFailedContainer && record.getMessage() != null && record.getMessage()
        .startsWith("Log output from the failed container");
  }

  private static boolean excludedExceptions(LogRecord record) {
    if (record.getThrown() == null) {
      return false;
    }
    return Stream.of(ContainerLaunchException.class)
        .anyMatch(clazz -> clazz.isAssignableFrom(record.getThrown().getClass()));
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
