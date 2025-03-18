package dev.streamx.cli;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.File;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@ApplicationScoped
public class LogsPurger {

  @Inject
  Logger logger;

  @ConfigProperty(name = "logs.purging.enabled", defaultValue = "false")
  boolean logsPurgingEnabled;

  void purge() {
    if (!logsPurgingEnabled) {
      logger.info("Purging logs disabled. Skipping purging.");
    }
    try {
      LocalDateTime now = LocalDateTime.now();
      LocalDateTime oneMonthAgo = now.minusMonths(1);
      Date threshold = Date.from(oneMonthAgo.atZone(ZoneId.systemDefault())
          .toInstant());
      long thresholdTime = threshold.getTime();

      String userHome = System.getProperty("user.home");

      Path logsLocation = Path.of(userHome + "/.streamx/logs").toAbsolutePath().normalize();
      File[] files = logsLocation.toFile().listFiles();

      for (File file : files) {
        if (file.getName().contains(".log") && file.lastModified() < thresholdTime) {
          file.delete();
        }
      }
    } catch (Exception e) {
      logger.error("Failed to purge logs", e);
    }
  }
}
