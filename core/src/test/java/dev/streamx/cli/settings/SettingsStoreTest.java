package dev.streamx.cli.settings;

import static org.assertj.core.api.Assertions.assertThat;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class SettingsStoreTest {

  @Inject
  SettingsStore cut;

  @AfterEach
  void tearDown() throws IOException {
    Files.deleteIfExists(
        Path.of("./target/test-classes/dev.streamx.cli.settings/new-settings.json"));
  }

  @Test
  void shouldNotFetchSettingIfSettingFileIsAbsent() {
    // when
    Optional<Setting> result = cut.retrieveSettings("nonexisting.json", Setting.class);

    // then
    assertThat(result).isEmpty();
  }

  @Test
  void shouldFetchSettingIfSettingFileExists() {
    // when
    Optional<Setting> result = cut.retrieveSettings("example-settings.json", Setting.class);

    // then
    assertThat(result).isPresent();
    assertThat(result.get().getValue()).isEqualTo("someValue");
  }

  @Test
  void shouldCreateFileIfNotExists() {
    // given
    String settingFile = "new-settings.json";
    Optional<Setting> nonExisting = cut.retrieveSettings(settingFile, Setting.class);
    assertThat(nonExisting).isEmpty();

    Setting setting = new Setting("value");

    // when
    cut.updateSettings(settingFile, setting);

    // then
    Optional<Setting> createdSettings = cut.retrieveSettings(settingFile, Setting.class);
    assertThat(createdSettings).isPresent();
    assertThat(createdSettings.get().getValue()).isEqualTo("value");
  }

  static class Setting {
    private String value;

    public Setting() {
    }

    public Setting(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }

    public void setValue(String value) {
      this.value = value;
    }
  }
}