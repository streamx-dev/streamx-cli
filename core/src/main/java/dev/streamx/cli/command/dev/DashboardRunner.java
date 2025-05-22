package dev.streamx.cli.command.dev;

import static dev.streamx.cli.util.Output.print;

import dev.streamx.cli.command.dev.event.DashboardStarted;
import dev.streamx.cli.util.StreamxMavenPropertiesUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import java.time.Duration;

@ApplicationScoped
public class DashboardRunner {
  private static final long CONTAINER_TIMEOUT_IN_SECS = 60_000L;

  @Inject
  DevConfig devConfig;

  @Inject
  BrowserOpener browserOpener;

  @Inject
  Event<DashboardStarted> dashboardStartedEvent;

  private DashboardContainer dashboardContainer;

  public void startStreamxDashboard(String meshPath, String meshDirectory,
      String projectDirectory) {
    dashboardContainer = new DashboardContainer(
        StreamxMavenPropertiesUtils.getDashboardImage(),
        devConfig.dashboardPort(),
        meshPath,
        meshDirectory,
        projectDirectory
    ).withStartupTimeout(Duration.ofSeconds(CONTAINER_TIMEOUT_IN_SECS));
    dashboardContainer.start();

    print("StreamX Dashboard started on http://localhost:" + devConfig.dashboardPort());

    browserOpener.tryOpenBrowser();
    dashboardStartedEvent.fire(new DashboardStarted());
  }

  public void stopStreamxDashboard() {
    dashboardContainer.stop();
    print("StreamX Dashboard stopped");
  }
}
