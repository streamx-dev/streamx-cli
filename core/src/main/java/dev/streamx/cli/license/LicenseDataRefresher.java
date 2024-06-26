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

/**
 * <p>
 * This class is responsible for maintaining License data and keeping it
 * as up-to-date as it's possible.
 * Basing on multiple factors
 * (last successful fetch date, "--accept-license" flag, "streamx.accept-license property)
 * it fetches license data if it's required or preferred.
 * If license data update is not required or update is preferred but e.g. some IO issues occurred,
 * it skips updating license data.
 * After license data are fetched, data is cached in local user settings.
 * </p>
 */
@ApplicationScoped
class LicenseDataRefresher {

  public static final int FETCH_NOT_REQUIRED_BEFORE_DURATION_IN_WEEKS = 1;
  public static final int FETCH_REQUIRED_AFTER_DURATION_IN_MONTHS = 3;

  @Inject
  LicenseSettingsStore licenseSettingsStore;

  @Inject
  LicenseConfig licenseConfig;

  @Inject
  LicenseFetcher licenseFetcher;

  LicenseSettings refresh(LocalDateTime now) {
    LicenseSettings licenseSettings = licenseSettingsStore.retrieveSettings();

    LicenseFetchRequired fetchRequired = isFetchRequired(licenseSettings, now);

    return updateSettings(fetchRequired, licenseSettings, now);
  }

  private LicenseFetchRequired isFetchRequired(LicenseSettings licenseSettings,
      LocalDateTime now) {
    Optional<LastLicenseFetch> lastLicenseFetch = licenseSettings.lastLicenseFetch();
    if (lastLicenseFetch.isEmpty()) {
      return licenseConfig.acceptLicense()
          ? PREFERRED
          : YES;
    } else {
      return resolveFetchRequiredWhenLicenseDataPresent(now, lastLicenseFetch.get());
    }
  }

  @NotNull
  private LicenseFetchRequired resolveFetchRequiredWhenLicenseDataPresent(LocalDateTime now,
      LastLicenseFetch lastLicenseFetch) {
    LocalDateTime lastFetchDate = lastLicenseFetch.fetchDate();

    if (isLastFetchInformationVeryOutdated(now, lastFetchDate)
        && !licenseConfig.acceptLicense()) {
      return YES;
    } else if (isLastFetchInformationLittleOutdated(now, lastFetchDate)) {
      return PREFERRED;
    } else {
      return NO;
    }
  }

  private static boolean isLastFetchInformationLittleOutdated(LocalDateTime now,
      LocalDateTime lastFetchDate) {
    return lastFetchDate.plusWeeks(FETCH_NOT_REQUIRED_BEFORE_DURATION_IN_WEEKS).isBefore(now);
  }

  private static boolean isLastFetchInformationVeryOutdated(LocalDateTime now,
      LocalDateTime lastFetchDate) {
    return lastFetchDate.plusMonths(FETCH_REQUIRED_AFTER_DURATION_IN_MONTHS).isBefore(now);
  }

  @NotNull
  private LicenseSettings updateSettings(LicenseFetchRequired fetchRequired,
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
    YES, // requires successful fetch
    PREFERRED, // fetch will be executed, however if it fails, it uses cached data
    NO, // fetch will be skipped
  }
}
