package dev.streamx.cli.license;

import dev.streamx.cli.exception.LicenseException;
import dev.streamx.cli.exception.PublicSettingsFileException;
import dev.streamx.cli.exception.SettingsFileException;
import dev.streamx.cli.license.input.AcceptingStrategy;
import dev.streamx.cli.license.model.LastLicenseFetch;
import dev.streamx.cli.license.model.LicenseSettings;
import dev.streamx.cli.license.proceeding.LicenseProceedingStrategy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import org.jboss.logging.Logger;
import org.jetbrains.annotations.NotNull;

@ApplicationScoped
public class LicenseProcessorEntrypoint {

  private static final String LICENSE = """
      Distributed under %s
      %s
      """;

  @Inject
  Logger log;

  @Inject
  LicenseProceedingStrategy licenseProceeding;

  @Inject
  LicenseDataRefresher licenseDataRefresher;

  @Inject
  LicenseAcceptanceVerifier licenseAcceptanceVerifier;

  @Inject
  LicenseSettingsStore licenseSettingsStore;

  @Inject
  AcceptingStrategy acceptingStrategy;

  @Inject
  LicenseContext licenseContext;

  public void process() {
    try {
      doProcess();
    } catch (SettingsFileException exception) {
      throw new PublicSettingsFileException(exception.getPathToSettings(), exception.getCause());
    }
  }

  private void doProcess() {
    if (!licenseProceeding.isEnabled()) {
      return;
    }

    LocalDateTime now = LocalDateTime.now();
    LicenseSettings refreshedLicenseSettings = licenseDataRefresher.refresh(now);
    boolean acceptanceRequired = licenseAcceptanceVerifier
        .isAcceptanceRequired(refreshedLicenseSettings);
    LicenseSettings licenseSettings = licenseSettingsStore.retrieveSettings();

    licenseSettings.lastLicenseFetch().ifPresent(settings -> print(getLicenseText(settings)));
    if (acceptanceRequired) {
      proceedLicenseAcceptance(licenseSettings, now);
    }
  }

  private void proceedLicenseAcceptance(LicenseSettings licenseSettings, LocalDateTime now) {
    print("");
    print("Do you accept the license agreement? [Y/n]");

    if (licenseContext.isAcceptLicenseFlagSet()) {
      proceedAutomaticLicenseAcceptance(licenseSettings, now);
      return;
    }

    if (acceptingStrategy.isLicenseAccepted()) {
      licenseSettingsStore.acceptLicense(licenseSettings, now);
    } else {
      throw LicenseException.licenseAcceptanceRejectedException();
    }
  }

  private void proceedAutomaticLicenseAcceptance(LicenseSettings licenseSettings,
      LocalDateTime now) {
    print("Y -> \"--accept-license\" was passed");
    if (licenseSettings.lastLicenseFetch().isPresent()) {
      licenseSettingsStore.acceptLicense(licenseSettings, now);
    } else {
      log.warn("Couldn't update accepted license data because of missing license data.");
    }
  }

  @NotNull
  private static String getLicenseText(LastLicenseFetch licenseSettings) {
    return LICENSE.formatted(
        licenseSettings.licenseName(),
        licenseSettings.licenseUrl()
    );
  }

  private static void print(String x) {
    System.out.println(x);
  }
}
