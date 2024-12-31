package dev.streamx.cli.command.cloud.deploy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dev.streamx.cli.command.cloud.ProjectUtils;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;

@QuarkusTest
class DataServiceTest {

  @Inject
  DataService cut;

  @Test
  void shouldReturnDataWithAllProperties() {
    Path propertiesPath = ProjectUtils.getResourcePath(Path.of("configs", "global.properties"));
    Map<String, String> data = cut.loadDataFromProperties(propertiesPath);
    Map<String, String> expectedProperties = Map.of(
        "CONFIG_GLOBAL_PROP_NAME", "config-global-prop-value",
        "CONFIG_GLOBAL_ANOTHER_PROP_NAME", "config-global-another-prop-value");
    assertThat(data).containsExactlyInAnyOrderEntriesOf(expectedProperties);
  }

  @Test
  void shouldThrowExceptionAboutInvalidPropertiesFile() {
    Path propertiesPath = ProjectUtils.getResourcePath(
        Path.of("configs", "nonexistent.properties"));
    RuntimeException nonexistentFileException = assertThrows(RuntimeException.class,
        () -> cut.loadDataFromProperties(propertiesPath));
    assertThat(nonexistentFileException.getMessage()).isEqualTo("Path " + propertiesPath
        + " provided in Mesh must be a valid properties file.");
    Path dirPath = ProjectUtils.getResourcePath(Path.of("configs"));
    RuntimeException dirIsNotValidPropertiesFileException = assertThrows(RuntimeException.class,
        () -> cut.loadDataFromProperties(dirPath));
    assertThat(dirIsNotValidPropertiesFileException.getMessage()).isEqualTo("Path " + dirPath
        + " provided in Mesh must be a valid properties file.");
  }

  @Test
  void shouldReturnDataMatchingFileContent() {
    Path propertiesPath = ProjectUtils.getResourcePath(Path.of("configs", "shared", "file.txt"));
    Map<String, String> data = cut.loadDataFromFiles(propertiesPath);
    Map<String, String> expectedData = Map.of("file.txt", "shared/file.txt");
    assertThat(data).containsExactlyInAnyOrderEntriesOf(expectedData);
  }

  @Test
  void shouldReturnDataMatchingDirContent() {
    Path propertiesPath = ProjectUtils.getResourcePath(Path.of("configs", "shared"));
    Map<String, String> data = cut.loadDataFromFiles(propertiesPath);
    Map<String, String> expectedData = Map.of(
        "file.txt", "shared/file.txt",
        "file1.txt", "shared/file1.txt");
    assertThat(data).containsExactlyInAnyOrderEntriesOf(expectedData);
  }
}