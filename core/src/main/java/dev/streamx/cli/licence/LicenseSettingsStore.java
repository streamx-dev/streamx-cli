package dev.streamx.cli.licence;

import dev.streamx.cli.licence.LicenceFetcher.Licence;
import dev.streamx.cli.licence.model.LastLicenceFetch;
import dev.streamx.cli.licence.model.LicenceApproval;
import dev.streamx.cli.licence.model.LicenceSettings;
import dev.streamx.cli.settings.SettingsStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
class LicenseSettingsStore {

  static final String LICENCE_SETTINGS_YAML = "licence-settings.yaml";

  @Inject
  SettingsStore settingsStore;

  LicenceSettings retrieveSettings() {
    return settingsStore.retrieveSettings(
        LICENCE_SETTINGS_YAML, LicenceSettings.class)
        .orElse(new LicenceSettings(Optional.empty(), List.of()));
  }

  LicenceSettings updateSettingsWithFetchedData(
      LicenceSettings licenceSettings,
      LocalDateTime now,
      Licence fetchedLicence
  ) {
    LastLicenceFetch lastLicenceFetch = new LastLicenceFetch(
        now,
        fetchedLicence.name(),
        fetchedLicence.url()
    );

    LicenceSettings updatedSettings =
        new LicenceSettings(Optional.of(lastLicenceFetch), licenceSettings.licenceApprovals());

    settingsStore.updateSettings(LICENCE_SETTINGS_YAML, updatedSettings);

    return updatedSettings;
  }

  LicenceSettings acceptLicence(
      LicenceSettings licenceSettings,
      LocalDateTime now
  ) {
    LastLicenceFetch lastLicenceFetch = licenceSettings.lastLicenceFetch()
        .orElseThrow(() ->
            new IllegalStateException("Updating accepting requires missing license data"));

    List<LicenceApproval> updatedLicences = new ArrayList<>(licenceSettings.licenceApprovals());
    LicenceApproval licenceApproval = new LicenceApproval(now,
        lastLicenceFetch.licenceName(), lastLicenceFetch.licenceUrl());
    updatedLicences.add(licenceApproval);

    LicenceSettings updatedSettings = new LicenceSettings(
        Optional.of(lastLicenceFetch),
        updatedLicences
    );

    settingsStore.updateSettings(LICENCE_SETTINGS_YAML, updatedSettings);

    return updatedSettings;
  }
}
