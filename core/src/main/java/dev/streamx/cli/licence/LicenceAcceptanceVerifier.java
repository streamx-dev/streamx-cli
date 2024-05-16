package dev.streamx.cli.licence;

import static dev.streamx.cli.licence.LicenceAcceptanceVerifier.LicenceFetchRequired.NO;
import static dev.streamx.cli.licence.LicenceAcceptanceVerifier.LicenceFetchRequired.PREFERRED;
import static dev.streamx.cli.licence.LicenceAcceptanceVerifier.LicenceFetchRequired.YES;

import dev.streamx.cli.exception.LicenceException;
import dev.streamx.cli.licence.LicenceFetcher.Licence;
import dev.streamx.cli.licence.model.LicenceSettings;
import dev.streamx.cli.settings.SettingsStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

@ApplicationScoped
public class LicenceAcceptanceVerifier {

  private static final String LICENCE_SETTINGS_YAML = "licence-settings.yaml";

  @Inject
  SettingsStore settingsStore;

  @Inject
  LicenceFetcher licenceFetcher;

  public boolean isAcceptanceRequired() {
    Optional<LicenceSettings> licenceSettings = settingsStore.retrieveSettings(
        LICENCE_SETTINGS_YAML, LicenceSettings.class);

    LocalDateTime now = LocalDateTime.now();
    LicenceFetchRequired fetchRequired = isFetchRequired(licenceSettings, now);
    LicenceSettings updatedSettings = updateSettingsWithFetchedData(fetchRequired,
        licenceSettings, now);

    String licenceUrlRequiredToBeAccepted = updatedSettings.lastFetchLicenceUrl();
    boolean requiredLicenceAccepted = updatedSettings.licenceApprovals().stream()
        .filter(Objects::nonNull)
        .anyMatch(licenceApproval ->
            StringUtils.equals(licenceApproval.url(), licenceUrlRequiredToBeAccepted));

    return !requiredLicenceAccepted;
  }

  private LicenceFetchRequired isFetchRequired(Optional<LicenceSettings> licenceSettings,
      LocalDateTime now) {
    if (licenceSettings.isEmpty()) {
      return YES;
    }
    LicenceSettings settings = licenceSettings.get();
    LocalDateTime lastFetchDate = settings.lastFetchDate();

    if (isLastFetchInformationVeryOutdated(now, lastFetchDate)) {
      return YES;
    } else if (isLastFetchInformationLittleOutdated(now, lastFetchDate)) {
      return PREFERRED;
    } else {
      return NO;
    }
  }

  private static boolean isLastFetchInformationLittleOutdated(LocalDateTime now,
      LocalDateTime lastFetchDate) {
    return lastFetchDate.plusWeeks(1).isBefore(now);
  }

  private static boolean isLastFetchInformationVeryOutdated(LocalDateTime now,
      LocalDateTime lastFetchDate) {
    return lastFetchDate.plusMonths(3).isBefore(now);
  }

  @NotNull
  private LicenceSettings updateSettingsWithFetchedData(LicenceFetchRequired fetchRequired,
      Optional<LicenceSettings> licenceSettings, LocalDateTime now) {
    if (fetchRequired == YES || fetchRequired == PREFERRED) {
      try {
        Licence fetchedLicence = licenceFetcher.fetchCurrentLicence();

        return updateSettingsWithFetchedData(licenceSettings, now, fetchedLicence);
      } catch (LicenceException licenceException) {
        if (fetchRequired == YES) {
          throw licenceException;
        } else {
          return licenceSettings.orElseThrow(() ->
              new IllegalStateException("Licence setting must exists if fetching is preferred."));
        }
      }
    } else {
      return licenceSettings.orElseThrow(() ->
          new IllegalStateException("Licence setting must exists if fetching is not required."));
    }
  }

  private LicenceSettings updateSettingsWithFetchedData(
      Optional<LicenceSettings> licenceSettings,
      LocalDateTime now,
      Licence fetchedLicence
  ) {
    LicenceSettings updatedSettings = licenceSettings
        .map(s -> new LicenceSettings(now, fetchedLicence.url(), s.licenceApprovals()))
        .orElseGet(() -> new LicenceSettings(now, fetchedLicence.url(),
            new ArrayList<>()));

    settingsStore.updateSettings(LICENCE_SETTINGS_YAML, updatedSettings);

    return updatedSettings;
  }

  enum LicenceFetchRequired {
    YES, PREFERRED, NO,
  }
}
