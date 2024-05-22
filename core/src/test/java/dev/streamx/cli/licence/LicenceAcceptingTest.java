package dev.streamx.cli.licence;

import static dev.streamx.cli.licence.LicenceWiremockConfigs.StandardWiremockLicence.LICENCE_NAME;
import static dev.streamx.cli.licence.LicenceWiremockConfigs.StandardWiremockLicence.LICENCE_URL;
import static org.assertj.core.api.Assertions.assertThat;

import dev.streamx.cli.licence.LicenceTestProfiles.AcceptingLicenceTestProfile;
import dev.streamx.cli.licence.LicenceTestProfiles.ProceedingTestProfile;
import dev.streamx.cli.licence.model.LastLicenceFetch;
import dev.streamx.cli.licence.model.LicenceApproval;
import dev.streamx.cli.licence.model.LicenceSettings;
import dev.streamx.cli.settings.SettingsStore;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestProfile(AcceptingLicenceTestProfile.class)
class LicenceAcceptingTest {

  public static final String OLD_URL = "http://old.streamx.dev/license.html";
  public static final String OLD_NAME = "oldLicense";

  LicenceArguments licenceArguments = new LicenceArguments();

  @Inject
  SettingsStore settingsStore;

  @Inject
  LicenceProcessorEntrypoint entrypoint;

  @BeforeEach
  void setup() {
    licenceArguments.propagateAcceptLicence(false);
  }

  @AfterEach
  void shutdown() {
    clearSettings();
  }

  @Test
  void shouldAcceptLicenseForClearEnvironment() {
    // given
    // clear environment

    // when
    entrypoint.process();

    // then
    LicenceSettings settings = verifyExistsLicense();
    verifyLastFetchedLicense(settings, LICENCE_NAME, LICENCE_URL);
    verifySingleApprovedLicense(settings, LICENCE_NAME, LICENCE_URL);
  }

  @Test
  void shouldSkipAcceptanceIfFlagAcceptLicenseWasGiven() {
    // given
    licenceArguments.propagateAcceptLicence(true);

    // when
    entrypoint.process();

    // then
    LicenceSettings settings = verifyExistsLicense();
    verifyLastFetchedLicense(settings, LICENCE_NAME, LICENCE_URL);
    assertThat(settings.licenceApprovals()).isEmpty();
  }

  @Test
  void shouldAcceptLicenseForNonAcceptedLicense() {
    // given
    LocalDateTime now = LocalDateTime.now();
    prepareGivenSettings(new LicenceSettings(
        Optional.of(new LastLicenceFetch(now, LICENCE_NAME, LICENCE_URL)),
        List.of()
    ));

    // when
    entrypoint.process();

    // then
    LicenceSettings settings = verifyExistsLicense();
    verifyLastFetchedLicense(settings, LICENCE_NAME, LICENCE_URL);
    verifySingleApprovedLicense(settings, LICENCE_NAME, LICENCE_URL);
  }

  @Test
  void shouldSkipAcceptanceIfLastLicenseDataWasRecently() {
    // given
    LocalDateTime longTimeAgo = LocalDateTime.now().minusDays(6);

    prepareGivenSettings(new LicenceSettings(
        Optional.of(new LastLicenceFetch(longTimeAgo, OLD_NAME, OLD_URL)),
        List.of(new LicenceApproval(longTimeAgo, OLD_NAME, OLD_URL))
    ));

    // when
    entrypoint.process();

    // then
    LicenceSettings settings = verifyExistsLicense();
    verifyLastFetchedLicense(settings, OLD_NAME, OLD_URL);
    verifySingleApprovedLicense(settings, OLD_NAME, OLD_URL);
  }

  @Test
  void shouldRequireAcceptanceIfLastLicenseDataWasLittleOld() {
    // given
    LocalDateTime longTimeAgo = LocalDateTime.now().minusDays(8);

    prepareGivenSettings(new LicenceSettings(
        Optional.of(new LastLicenceFetch(longTimeAgo, OLD_NAME, OLD_URL)),
        List.of(new LicenceApproval(longTimeAgo, OLD_NAME, OLD_URL))
    ));

    // when
    entrypoint.process();

    // then
    LicenceSettings settings = verifyExistsLicense();
    verifyLastFetchedLicense(settings, LICENCE_NAME, LICENCE_URL);
    verifyTwoApprovedLicense(settings, LICENCE_NAME, LICENCE_URL);
  }

  private void prepareGivenSettings(LicenceSettings givenSettings) {
    settingsStore.updateSettings(LicenseSettingsStore.LICENCE_SETTINGS_YAML, givenSettings);
  }

  private static void verifySingleApprovedLicense(LicenceSettings settings,
      String licenceName, String licenceUrl) {
    List<LicenceApproval> licenceApprovals = settings.licenceApprovals();
    assertThat(licenceApprovals).hasSize(1);
    verifyContainsLicense(licenceName, licenceUrl, licenceApprovals);
  }

  private static void verifyTwoApprovedLicense(LicenceSettings settings,
      String licenceName, String licenceUrl) {
    List<LicenceApproval> licenceApprovals = settings.licenceApprovals();
    assertThat(licenceApprovals).hasSize(2);
    verifyContainsLicense(licenceName, licenceUrl, licenceApprovals);
  }

  private static void verifyContainsLicense(String licenceName, String licenceUrl,
      List<LicenceApproval> licenceApprovals) {
    LicenceApproval licenceApproval = licenceApprovals.stream()
        .filter(approval -> licenceName.equals(approval.name()))
        .findFirst()
        .orElseThrow();
    assertThat(licenceApproval.name()).isEqualTo(licenceName);
    assertThat(licenceApproval.url()).isEqualTo(licenceUrl);
  }

  private static void verifyLastFetchedLicense(LicenceSettings settings,
      String licenceName, String licenceUrl) {
    Optional<LastLicenceFetch> lastLicenceFetch = settings.lastLicenceFetch();
    assertThat(lastLicenceFetch).isPresent();
    assertThat(lastLicenceFetch.get().licenceName()).isEqualTo(licenceName);
    assertThat(lastLicenceFetch.get().licenceUrl()).isEqualTo(licenceUrl);
  }

  @NotNull
  private LicenceSettings verifyExistsLicense() {
    Optional<LicenceSettings> licenseSettingsStore = settingsStore.retrieveSettings(
        LicenseSettingsStore.LICENCE_SETTINGS_YAML,
        LicenceSettings.class);
    assertThat(licenseSettingsStore).isPresent();
    LicenceSettings settings = licenseSettingsStore.get();
    return settings;
  }


  private static void clearSettings() {
    try {
      Path pathToBeDeleted = Path.of(ProceedingTestProfile.TEST_SETTINGS_PATH_ROOT);

      Files.walk(pathToBeDeleted)
          .sorted(Comparator.reverseOrder())
          .map(Path::toFile)
          .forEach(File::delete);
    } catch (IOException e) {
      // skip
    }
  }
}