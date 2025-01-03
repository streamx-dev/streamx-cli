package dev.streamx.cli.command.cloud.deploy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import dev.streamx.cli.command.cloud.ProjectPathsResolver;
import dev.streamx.cli.command.cloud.ProjectUtils;
import dev.streamx.cli.command.cloud.deploy.Config.ConfigType;
import io.quarkus.test.InjectMock;
import io.quarkus.test.component.QuarkusComponentTest;
import jakarta.inject.Inject;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;

@QuarkusComponentTest
class ConfigServiceTest {

  @Inject
  ConfigService cut;

  @InjectMock
  ProjectPathsResolver projectPathsResolver;

  @InjectMock
  DataService dataService;

  @Test
  void shouldReturnDirType() {
    ConfigType configType = cut.getConfigType(
        ProjectUtils.getResourcePath(Path.of("configs", "shared")));
    assertEquals(ConfigType.DIR, configType);
  }

  @Test
  void shouldReturnFileType() {
    ConfigType configType = cut.getConfigType(
        ProjectUtils.getResourcePath(Path.of("configs", "global.properties")));
    assertEquals(ConfigType.FILE, configType);
  }

  @Test
  void shouldReturnCorrectLabelValue() {
    assertEquals("file", ConfigType.FILE.getLabelValue());
    assertEquals("dir", ConfigType.DIR.getLabelValue());
  }

  @Test
  void shouldReturnEnvConfig() {
    Path projectPath = ProjectUtils.getProjectPath();
    String configName = "global.properties";
    Path configPath = ProjectUtils.getResourcePath(Path.of("configs", configName));
    when(projectPathsResolver.resolveConfigPath(projectPath, configName)).thenReturn(configPath);
    Map<String, String> data = Map.of("key", "value");
    when(dataService.loadDataFromProperties(configPath)).thenReturn(data);
    Config configEnv = cut.getConfigEnv(projectPath, configName);
    assertEquals(configEnv.name(), configName);
    assertEquals(configEnv.data(), data);
    assertEquals(configEnv.configType(), ConfigType.FILE);
  }

  @Test
  void shouldReturnSecretEnvConfig() {
    Path projectPath = ProjectUtils.getProjectPath();
    String configName = "shared.properties";
    Path configPath = ProjectUtils.getResourcePath(Path.of("secrets", configName));
    when(projectPathsResolver.resolveSecretPath(projectPath, configName)).thenReturn(configPath);
    Map<String, String> data = Map.of("secretKey", "secretValue");
    when(dataService.loadDataFromProperties(configPath)).thenReturn(data);
    Config configEnv = cut.getSecretEnv(projectPath, configName);
    assertEquals(configEnv.name(), configName);
    assertEquals(configEnv.data(), data);
    assertEquals(configEnv.configType(), ConfigType.FILE);
  }

  @Test
  void shouldReturnFileVolumeConfig() {
    Path projectPath = ProjectUtils.getProjectPath();
    String configName = "delivery/wds/file.txt";
    Path configPath = ProjectUtils.getResourcePath(Path.of("configs", configName));
    when(projectPathsResolver.resolveConfigPath(projectPath, configName)).thenReturn(configPath);
    Map<String, String> data = Map.of("file.txt", "File content");
    when(dataService.loadDataFromFiles(configPath)).thenReturn(data);
    Config configEnv = cut.getConfigVolume(projectPath, configName);
    assertEquals(configEnv.name(), configName);
    assertEquals(configEnv.data(), data);
    assertEquals(configEnv.configType(), ConfigType.FILE);
  }

  @Test
  void shouldReturnDirVolumeConfig() {
    Path projectPath = ProjectUtils.getProjectPath();
    String configName = "delivery/wds/dir";
    Path configPath = ProjectUtils.getResourcePath(Path.of("configs", configName));
    when(projectPathsResolver.resolveConfigPath(projectPath, configName)).thenReturn(configPath);
    Map<String, String> data = Map.of("file.txt", "File content", "file1.txt", "File1 content");
    when(dataService.loadDataFromFiles(configPath)).thenReturn(data);
    Config configEnv = cut.getConfigVolume(projectPath, configName);
    assertEquals(configEnv.name(), configName);
    assertEquals(configEnv.data(), data);
    assertEquals(configEnv.configType(), ConfigType.DIR);
  }

  @Test
  void shouldReturnSecretFileVolumeConfig() {
    Path projectPath = ProjectUtils.getProjectPath();
    String configName = "delivery/wds/file.txt";
    Path configPath = ProjectUtils.getResourcePath(Path.of("secrets", configName));
    when(projectPathsResolver.resolveSecretPath(projectPath, configName)).thenReturn(configPath);
    Map<String, String> data = Map.of("secret-file.txt", "File content");
    when(dataService.loadDataFromFiles(configPath)).thenReturn(data);
    Config configEnv = cut.getSecretVolume(projectPath, configName);
    assertEquals(configEnv.name(), configName);
    assertEquals(configEnv.data(), data);
    assertEquals(configEnv.configType(), ConfigType.FILE);
  }

  @Test
  void shouldReturnSecretDirVolumeConfig() {
    Path projectPath = ProjectUtils.getProjectPath();
    String configName = "delivery/wds/dir";
    Path configPath = ProjectUtils.getResourcePath(Path.of("secrets", configName));
    when(projectPathsResolver.resolveSecretPath(projectPath, configName)).thenReturn(configPath);
    Map<String, String> data = Map.of("secret-file.txt", "File content", "secret-file1.txt",
        "File1 content");
    when(dataService.loadDataFromFiles(configPath)).thenReturn(data);
    Config configEnv = cut.getSecretVolume(projectPath, configName);
    assertEquals(configEnv.name(), configName);
    assertEquals(configEnv.data(), data);
    assertEquals(configEnv.configType(), ConfigType.DIR);
  }
}