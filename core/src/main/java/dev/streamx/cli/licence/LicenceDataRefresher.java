package dev.streamx.cli.licence;

import static dev.streamx.cli.licence.LicenceDataRefresher.LicenceFetchRequired.NO;
import static dev.streamx.cli.licence.LicenceDataRefresher.LicenceFetchRequired.PREFERRED;
import static dev.streamx.cli.licence.LicenceDataRefresher.LicenceFetchRequired.YES;

import dev.streamx.cli.exception.LicenceException;
import dev.streamx.cli.licence.LicenceFetcher.Licence;
import dev.streamx.cli.licence.model.LastLicenceFetch;
import dev.streamx.cli.licence.model.LicenceSettings;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

@ApplicationScoped
class LicenceDataRefresher {

  @Inject
  LicenseSettingsStore licenseSettingsStore;

  @Inject
  LicenceContext licenceContext;

  @Inject
  LicenceFetcher licenceFetcher;

  LicenceSettings refresh(LocalDateTime now) {
    LicenceSettings licenceSettings = licenseSettingsStore.retrieveSettings();

    LicenceFetchRequired fetchRequired = isFetchRequired(licenceSettings, now);

    return updateSettingsWithFetchedData(fetchRequired, licenceSettings, now);
  }

  private LicenceFetchRequired isFetchRequired(LicenceSettings licenceSettings,
      LocalDateTime now) {
    if (licenceContext.isAcceptLicence()) {
      return PREFERRED;
    }

    Optional<LastLicenceFetch> lastLicenceFetch = licenceSettings.lastLicenceFetch();
    if (lastLicenceFetch.isEmpty()) {
      return YES;
    } else {
      LocalDateTime lastFetchDate = lastLicenceFetch.get().fetchDate();

      if (isLastFetchInformationVeryOutdated(now, lastFetchDate)) {
        return YES;
      } else if (isLastFetchInformationLittleOutdated(now, lastFetchDate)) {
        return PREFERRED;
      } else {
        return NO;
      }
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
      LicenceSettings licenceSettings, LocalDateTime now) {
    if (fetchRequired == YES || fetchRequired == PREFERRED) {
      try {
        Licence fetchedLicence = licenceFetcher.fetchCurrentLicence();

        return licenseSettingsStore.updateSettingsWithFetchedData(
            licenceSettings, now, fetchedLicence);
      } catch (LicenceException licenceException) {
        if (fetchRequired == YES) {
          throw licenceException;
        } else {
          return licenceSettings;
        }
      }
    } else {
      return licenceSettings;
    }
  }

  public enum LicenceFetchRequired {
    YES, PREFERRED, NO,
  }
}
