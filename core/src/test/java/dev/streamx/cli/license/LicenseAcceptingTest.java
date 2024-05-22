package dev.streamx.cli.license;

import static dev.streamx.cli.license.LicenseWiremockConfigs.StandardWiremockLicense.LICENSE_NAME;
import static dev.streamx.cli.license.LicenseWiremockConfigs.StandardWiremockLicense.LICENSE_URL;
import static org.assertj.core.api.Assertions.assertThat;

import dev.streamx.cli.license.LicenseTestProfiles.AcceptingLicenseTestProfile;
import dev.streamx.cli.license.LicenseTestProfiles.ProceedingTestProfile;
import dev.streamx.cli.license.model.LastLicenseFetch;
import dev.streamx.cli.license.model.LicenseApproval;
import dev.streamx.cli.license.model.LicenseSettings;
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
@TestProfile(AcceptingLicenseTestProfile.class)
class LicenseAcceptingTest {

  public static final String OLD_URL = "http://old.streamx.dev/license.html";
  public static final String OLD_NAME = "oldLicense";

  LicenseArguments licenseArguments = new LicenseArguments();

  @Inject
  SettingsStore settingsStore;

  @Inject
  LicenseProcessorEntrypoint entrypoint;

  @BeforeEach
  void setup() {
    licenseArguments.propagateAcceptLicense(false);
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
    LicenseSettings settings = verifyExistsLicense();
    verifyLastFetchedLicense(settings, LICENSE_NAME, LICENSE_URL);
    verifySingleApprovedLicense(settings, LICENSE_NAME, LICENSE_URL);
  }

  @Test
  void shouldSkipAcceptanceIfFlagAcceptLicenseWasGiven() {
    // given
    licenseArguments.propagateAcceptLicense(true);

    // when
    entrypoint.process();

    // then
    LicenseSettings settings = verifyExistsLicense();
    verifyLastFetchedLicense(settings, LICENSE_NAME, LICENSE_URL);
    assertThat(settings.licenseApprovals()).isEmpty();
  }

  @Test
  void shouldAcceptLicenseForNonAcceptedLicense() {
    // given
    LocalDateTime now = LocalDateTime.now();
    prepareGivenSettings(new LicenseSettings(
        Optional.of(new LastLicenseFetch(now, LICENSE_NAME, LICENSE_URL)),
        List.of()
    ));

    // when
    entrypoint.process();

    // then
    LicenseSettings settings = verifyExistsLicense();
    verifyLastFetchedLicense(settings, LICENSE_NAME, LICENSE_URL);
    verifySingleApprovedLicense(settings, LICENSE_NAME, LICENSE_URL);
  }

  @Test
  void shouldSkipAcceptanceIfLastLicenseDataWasRecently() {
    // given
    LocalDateTime longTimeAgo = LocalDateTime.now().minusDays(6);

    prepareGivenSettings(new LicenseSettings(
        Optional.of(new LastLicenseFetch(longTimeAgo, OLD_NAME, OLD_URL)),
        List.of(new LicenseApproval(longTimeAgo, OLD_NAME, OLD_URL))
    ));

    // when
    entrypoint.process();

    // then
    LicenseSettings settings = verifyExistsLicense();
    verifyLastFetchedLicense(settings, OLD_NAME, OLD_URL);
    verifySingleApprovedLicense(settings, OLD_NAME, OLD_URL);
  }

  @Test
  void shouldRequireAcceptanceIfLastLicenseDataWasLittleOld() {
    // given
    LocalDateTime longTimeAgo = LocalDateTime.now().minusDays(8);

    prepareGivenSettings(new LicenseSettings(
        Optional.of(new LastLicenseFetch(longTimeAgo, OLD_NAME, OLD_URL)),
        List.of(new LicenseApproval(longTimeAgo, OLD_NAME, OLD_URL))
    ));

    // when
    entrypoint.process();

    // then
    LicenseSettings settings = verifyExistsLicense();
    verifyLastFetchedLicense(settings, LICENSE_NAME, LICENSE_URL);
    verifyTwoApprovedLicense(settings, LICENSE_NAME, LICENSE_URL);
  }

  private void prepareGivenSettings(LicenseSettings givenSettings) {
    settingsStore.updateSettings(LicenseSettingsStore.LICENSE_SETTINGS_YAML, givenSettings);
  }

  private static void verifySingleApprovedLicense(LicenseSettings settings,
      String licenseName, String licenseUrl) {
    List<LicenseApproval> licenseApprovals = settings.licenseApprovals();
    assertThat(licenseApprovals).hasSize(1);
    verifyContainsLicense(licenseName, licenseUrl, licenseApprovals);
  }

  private static void verifyTwoApprovedLicense(LicenseSettings settings,
      String licenseName, String licenseUrl) {
    List<LicenseApproval> licenseApprovals = settings.licenseApprovals();
    assertThat(licenseApprovals).hasSize(2);
    verifyContainsLicense(licenseName, licenseUrl, licenseApprovals);
  }

  private static void verifyContainsLicense(String licenseName, String licenseUrl,
      List<LicenseApproval> licenseApprovals) {
    LicenseApproval licenseApproval = licenseApprovals.stream()
        .filter(approval -> licenseName.equals(approval.name()))
        .findFirst()
        .orElseThrow();
    assertThat(licenseApproval.name()).isEqualTo(licenseName);
    assertThat(licenseApproval.url()).isEqualTo(licenseUrl);
  }

  private static void verifyLastFetchedLicense(LicenseSettings settings,
      String licenseName, String licenseUrl) {
    Optional<LastLicenseFetch> lastLicenseFetch = settings.lastLicenseFetch();
    assertThat(lastLicenseFetch).isPresent();
    assertThat(lastLicenseFetch.get().licenseName()).isEqualTo(licenseName);
    assertThat(lastLicenseFetch.get().licenseUrl()).isEqualTo(licenseUrl);
  }

  @NotNull
  private LicenseSettings verifyExistsLicense() {
    Optional<LicenseSettings> licenseSettingsStore = settingsStore.retrieveSettings(
        LicenseSettingsStore.LICENSE_SETTINGS_YAML,
        LicenseSettings.class);
    assertThat(licenseSettingsStore).isPresent();
    LicenseSettings settings = licenseSettingsStore.get();
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