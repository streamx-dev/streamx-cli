package dev.streamx.cli.license;

import dev.streamx.cli.license.LicenseFetcher.License;
import dev.streamx.cli.license.model.LastLicenseFetch;
import dev.streamx.cli.license.model.LicenseApproval;
import dev.streamx.cli.license.model.LicenseSettings;
import dev.streamx.cli.settings.SettingsStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
class LicenseSettingsStore {

  static final String LICENSE_YML = "license.yml";

  @Inject
  SettingsStore settingsStore;

  LicenseSettings retrieveSettings() {
    return settingsStore.retrieveSettings(LICENSE_YML, LicenseSettings.class)
        .orElse(new LicenseSettings(Optional.empty(), List.of()));
  }

  LicenseSettings updateSettingsWithFetchedData(
      LicenseSettings licenseSettings,
      LocalDateTime now,
      License fetchedLicense
  ) {
    LastLicenseFetch lastLicenseFetch = new LastLicenseFetch(
        now,
        fetchedLicense.name(),
        fetchedLicense.url()
    );

    LicenseSettings updatedSettings =
        new LicenseSettings(Optional.of(lastLicenseFetch), licenseSettings.licenseApprovals());

    settingsStore.updateSettings(LICENSE_YML, updatedSettings);

    return updatedSettings;
  }

  void acceptLicense(LicenseSettings licenseSettings, LocalDateTime now) {
    LastLicenseFetch lastLicenseFetch = licenseSettings.lastLicenseFetch()
        .orElseThrow(() ->
            new IllegalStateException("Accepting license requires license data (which is absent)"));

    List<LicenseApproval> updatedLicenses = new ArrayList<>(licenseSettings.licenseApprovals());
    LicenseApproval licenseApproval = new LicenseApproval(now,
        lastLicenseFetch.licenseName(), lastLicenseFetch.licenseUrl());
    updatedLicenses.add(licenseApproval);

    LicenseSettings updatedSettings = new LicenseSettings(
        Optional.of(lastLicenseFetch),
        updatedLicenses
    );

    settingsStore.updateSettings(LICENSE_YML, updatedSettings);
  }
}
