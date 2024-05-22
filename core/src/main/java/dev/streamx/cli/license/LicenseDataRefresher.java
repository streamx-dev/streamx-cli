package dev.streamx.cli.license;

import static dev.streamx.cli.license.LicenseDataRefresher.LicenseFetchRequired.NO;
import static dev.streamx.cli.license.LicenseDataRefresher.LicenseFetchRequired.PREFERRED;
import static dev.streamx.cli.license.LicenseDataRefresher.LicenseFetchRequired.YES;

import dev.streamx.cli.exception.LicenseException;
import dev.streamx.cli.license.LicenseFetcher.License;
import dev.streamx.cli.license.model.LastLicenseFetch;
import dev.streamx.cli.license.model.LicenseSettings;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

@ApplicationScoped
class LicenseDataRefresher {

  @Inject
  LicenseSettingsStore licenseSettingsStore;

  @Inject
  LicenseContext licenseContext;

  @Inject
  LicenseFetcher licenseFetcher;

  LicenseSettings refresh(LocalDateTime now) {
    LicenseSettings licenseSettings = licenseSettingsStore.retrieveSettings();

    LicenseFetchRequired fetchRequired = isFetchRequired(licenseSettings, now);

    return updateSettingsWithFetchedData(fetchRequired, licenseSettings, now);
  }

  private LicenseFetchRequired isFetchRequired(LicenseSettings licenseSettings,
      LocalDateTime now) {
    if (licenseContext.isAcceptLicenseFlagPresent()) {
      return PREFERRED;
    }

    Optional<LastLicenseFetch> lastLicenseFetch = licenseSettings.lastLicenseFetch();
    if (lastLicenseFetch.isEmpty()) {
      return YES;
    } else {
      LocalDateTime lastFetchDate = lastLicenseFetch.get().fetchDate();

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
  private LicenseSettings updateSettingsWithFetchedData(LicenseFetchRequired fetchRequired,
      LicenseSettings licenseSettings, LocalDateTime now) {
    if (fetchRequired == YES || fetchRequired == PREFERRED) {
      try {
        License fetchedLicense = licenseFetcher.fetchCurrentLicense();

        return licenseSettingsStore.updateSettingsWithFetchedData(
            licenseSettings, now, fetchedLicense);
      } catch (LicenseException licenseException) {
        if (fetchRequired == YES) {
          throw licenseException;
        } else {
          return licenseSettings;
        }
      }
    } else {
      return licenseSettings;
    }
  }

  public enum LicenseFetchRequired {
    YES, PREFERRED, NO,
  }
}
