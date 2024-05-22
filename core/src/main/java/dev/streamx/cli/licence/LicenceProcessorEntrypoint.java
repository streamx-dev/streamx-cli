package dev.streamx.cli.licence;

import dev.streamx.cli.exception.LicenceException;
import dev.streamx.cli.exception.PublicSettingsFileException;
import dev.streamx.cli.exception.SettingsFileException;
import dev.streamx.cli.licence.input.AcceptingStrategy;
import dev.streamx.cli.licence.model.LastLicenceFetch;
import dev.streamx.cli.licence.model.LicenceAcceptingStrategy;
import dev.streamx.cli.licence.model.LicenceSettings;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jetbrains.annotations.NotNull;

@ApplicationScoped
public class LicenceProcessorEntrypoint {

  private static final String LICENCE = """
      Distributed under %s
      %s
      """;

  @ConfigProperty(name = "streamx.cli.licence.strategy")
  LicenceAcceptingStrategy licenceAcceptingStrategy;

  @Inject
  LicenceDataRefresher licenceDataRefresher;

  @Inject
  LicenceAcceptanceVerifier licenceAcceptanceVerifier;

  @Inject
  LicenseSettingsStore licenseSettingsStore;

  @Inject
  AcceptingStrategy acceptingStrategy;

  public void process() {
    try {
      doProcess();
    } catch (SettingsFileException exception) {
      throw new PublicSettingsFileException(exception.getPathToSettings(), exception.getCause());
    }
  }

  private void doProcess() {
    if (licenceAcceptingStrategy == LicenceAcceptingStrategy.SKIP) {
      return;
    }

    LocalDateTime now = LocalDateTime.now();
    LicenceSettings refreshedLicenceSettings = licenceDataRefresher.refresh(now);
    boolean acceptanceRequired = licenceAcceptanceVerifier
        .isAcceptanceRequired(refreshedLicenceSettings);
    LicenceSettings licenceSettings = licenseSettingsStore.retrieveSettings();

    licenceSettings.lastLicenceFetch().ifPresent(settings -> print(getLicenceText(settings)));
    if (acceptanceRequired) {
      proceedLicenseAcceptance(licenceSettings, now);
    }
  }

  private void proceedLicenseAcceptance(LicenceSettings licenceSettings, LocalDateTime now) {
    print("");
    print("Do you accept the license agreement? [Y/n]");

    if (acceptingStrategy.isLicenceAccepted()) {
      licenseSettingsStore.acceptLicence(licenceSettings, now);
    } else {
      throw LicenceException.licenceAcceptanceRejectedException();
    }
  }

  @NotNull
  private static String getLicenceText(LastLicenceFetch licenceSettings) {
    return LICENCE.formatted(
        licenceSettings.licenceName(),
        licenceSettings.licenceUrl()
    );
  }

  private static void print(String x) {
    System.out.println(x);
  }
}
