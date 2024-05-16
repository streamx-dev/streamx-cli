package dev.streamx.cli.licence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import dev.streamx.cli.exception.LicenceException;
import dev.streamx.cli.licence.LicenceFetcher.Licence;
import dev.streamx.cli.licence.model.LicenceApproval;
import dev.streamx.cli.licence.model.LicenceSettings;
import dev.streamx.cli.settings.SettingsStore;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

@QuarkusTest
class LicenceAcceptanceVerifierTest {

  @InjectMock
  SettingsStore settingsStore;

  @InjectMock
  LicenceFetcher licenceFetcher;

  @Inject
  LicenceAcceptanceVerifier cut;

  @Test
  void shouldRequireAcceptanceForSettingsMissing() {
    // given
    when(settingsStore.retrieveSettings(any(), any())).thenReturn(Optional.empty());
    when(licenceFetcher.fetchCurrentLicence()).thenReturn(new Licence("name", "url"));

    // when
    boolean result = cut.isAcceptanceRequired();

    // then
    assertThat(result).isTrue();
  }

  @Test
  void shouldRequireAcceptanceIfPreviousWasNotAccepted() {
    // given
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime lastFetchDate = consideredAsUpToDate(now);
    String url = "url";
    LicenceSettings storedSettings = new LicenceSettings(lastFetchDate, url, List.of());
    when(settingsStore.retrieveSettings(any(), any())).thenReturn(Optional.of(storedSettings));

    // when
    boolean result = cut.isAcceptanceRequired();

    // then
    assertThat(result).isTrue();
  }

  @Test
  void shouldNotRequireAcceptanceIfPreviousLicenceWasAccepted() {
    // given
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime lastFetchDate = consideredAsUpToDate(now);
    String url = "url";
    LicenceSettings storedLicenceSettings = new LicenceSettings(lastFetchDate, url, List.of(
        new LicenceApproval(lastFetchDate, "name", url)
    ));
    when(settingsStore.retrieveSettings(any(), any()))
        .thenReturn(Optional.of(storedLicenceSettings));

    // when
    boolean result = cut.isAcceptanceRequired();

    // then
    assertThat(result).isFalse();
  }

  @Test
  void shouldNotRequireAcceptanceIfLicenceDidNotChange() {
    // given
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime lastFetchDate = littleOutdatedDate(now);
    String url = "url";
    LicenceSettings storedLicenceSettings = new LicenceSettings(lastFetchDate, url, List.of(
        new LicenceApproval(lastFetchDate, "name", url)
    ));
    when(settingsStore.retrieveSettings(any(), any()))
        .thenReturn(Optional.of(storedLicenceSettings));
    when(licenceFetcher.fetchCurrentLicence()).thenReturn(new Licence("name", url));

    // when
    boolean result = cut.isAcceptanceRequired();

    // then
    assertThat(result).isFalse();
  }

  @Test
  void shouldRequireAcceptanceIfLicenceDidChange() {
    // given
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime lastFetchDate = littleOutdatedDate(now);
    String url = "url";
    String fetchedLicenceUrl = "newUrl";
    LicenceSettings storedLicenceSettings = new LicenceSettings(lastFetchDate, url, List.of(
        new LicenceApproval(lastFetchDate, "name", url)
    ));
    when(settingsStore.retrieveSettings(any(), any()))
        .thenReturn(Optional.of(storedLicenceSettings));
    when(licenceFetcher.fetchCurrentLicence()).thenReturn(new Licence("name", fetchedLicenceUrl));

    // when
    boolean result = cut.isAcceptanceRequired();

    // then
    assertThat(result).isTrue();
  }

  @Test
  void shouldSkipVerificationDuringConnectionIssuesForLittleOutdatedLicenceData()
      throws URISyntaxException {
    // given
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime lastFetchDate = littleOutdatedDate(now);
    String url = "url";
    LicenceSettings storedLicenceSettings = new LicenceSettings(lastFetchDate, url, List.of(
        new LicenceApproval(lastFetchDate, "name", url)
    ));
    when(settingsStore.retrieveSettings(any(), any()))
        .thenReturn(Optional.of(storedLicenceSettings));
    when(licenceFetcher.fetchCurrentLicence()).thenThrow(
        LicenceException.licenceFetchException(new URI("http://localhost:8080")));

    // when
    boolean result = cut.isAcceptanceRequired();

    // then
    assertThat(result).isFalse();
  }

  @Test
  void shouldRequireVerificationDuringConnectionIssuesForVeryOutdatedLicenceData()
      throws URISyntaxException {
    // given
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime lastFetchDate = veryOutdatedDate(now);
    String url = "url";
    LicenceSettings storedLicenceSettings = new LicenceSettings(lastFetchDate, url, List.of(
        new LicenceApproval(lastFetchDate, "name", url)
    ));
    when(settingsStore.retrieveSettings(any(), any()))
        .thenReturn(Optional.of(storedLicenceSettings));
    when(licenceFetcher.fetchCurrentLicence()).thenThrow(
        LicenceException.licenceFetchException(new URI("http://localhost:8080")));

    // when
    Exception exception = catchException(() -> cut.isAcceptanceRequired());

    // then
    assertThat(exception).isInstanceOf(LicenceException.class);
  }

  @NotNull
  private static LocalDateTime consideredAsUpToDate(LocalDateTime now) {
    return now.minusDays(6);
  }

  @NotNull
  private static LocalDateTime littleOutdatedDate(LocalDateTime now) {
    return now.minusDays(10);
  }

  @NotNull
  private static LocalDateTime veryOutdatedDate(LocalDateTime now) {
    return now.minusMonths(4);
  }
}