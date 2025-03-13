package dev.streamx.cli.command.dev;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.awt.Desktop;
import java.net.URI;
import org.jboss.logging.Logger;

@ApplicationScoped
public class BrowserOpener {

  @Inject
  Logger logger;

  @Inject
  DevConfig devConfig;

  public void tryOpenBrowser() {
    if (!devConfig.openOnStart()) {
      return;
    }

    try {
      if (Desktop.isDesktopSupported()) {
        Desktop desktop = Desktop.getDesktop();
        URI meshManagerUri = new URI("http://localhost:" + devConfig.dashboardPort());
        desktop.browse(meshManagerUri);
      } else {
        logger.warn("Opening browser is not supported");
      }
    } catch (Exception e) {
      logger.error("Opening browser failed", e);
    }
  }
}
