package dev.streamx.cli.command.cloud.deploy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import dev.streamx.cli.command.cloud.KubernetesService;
import dev.streamx.cli.command.cloud.ProjectUtils;
import dev.streamx.cli.command.cloud.ServiceMeshService.ConfigSourcesPaths;
import dev.streamx.cli.command.cloud.deploy.Config.ConfigType;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.Secret;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

@QuarkusTest
class ProjectResourcesExtractorTest {

  @InjectMock
  ConfigService configService;

  @InjectMock
  KubernetesService kubernetesService;

  @Inject
  ProjectResourcesExtractor projectResourcesExtractor;

  @Test
  void shouldReturnExtractedConfigMaps() {
    String meshName = "sx";
    Path projectPath = ProjectUtils.getProjectPath();
    ConfigSourcesPaths configSourcesPaths = new ConfigSourcesPaths(
        Set.of("global.properties"),
        Collections.emptySet(),
        Set.of("shared/file.txt"),
        Collections.emptySet());
    Config config = new Config("name", Map.of("key", "value"), ConfigType.FILE);
    when(configService.getConfigEnv(projectPath, "global.properties")).thenReturn(config);
    when(configService.getConfigVolume(projectPath, "shared/file.txt")).thenReturn(config);
    when(kubernetesService.buildConfigMap(meshName, config)).thenReturn(mock(ConfigMap.class));
    List<ConfigMap> configMaps = projectResourcesExtractor.getConfigMaps(projectPath,
        configSourcesPaths, meshName);
    assertThat(configMaps).hasSize(2);
  }

  @Test
  void shouldReturnExtractedSecrets() {
    String meshName = "sx";
    Path projectPath = ProjectUtils.getProjectPath();
    ConfigSourcesPaths configSourcesPaths = new ConfigSourcesPaths(
        Collections.emptySet(),
        Set.of("global.properties"),
        Collections.emptySet(),
        Set.of("shared/file.txt"));
    Config config = new Config("name", Map.of("key", "value"), ConfigType.FILE);
    when(configService.getSecretEnv(projectPath, "global.properties")).thenReturn(config);
    when(configService.getSecretVolume(projectPath, "shared/file.txt")).thenReturn(config);
    when(kubernetesService.buildSecret(meshName, config)).thenReturn(mock(Secret.class));
    List<Secret> secrets = projectResourcesExtractor.getSecrets(projectPath,
        configSourcesPaths, meshName);
    assertThat(secrets).hasSize(2);
  }
}