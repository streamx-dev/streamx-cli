package dev.streamx.cli.command.cloud.deploy;

import static dev.streamx.cli.command.cloud.deploy.KubernetesService.COMPONENT_EXTERNAL_CONFIG;
import static dev.streamx.cli.command.cloud.deploy.KubernetesService.COMPONENT_EXTERNAL_SECRET;
import static dev.streamx.cli.command.cloud.deploy.KubernetesService.COMPONENT_LABEL;
import static dev.streamx.cli.command.cloud.deploy.KubernetesService.CONFIG_TYPE_LABEL;
import static dev.streamx.cli.command.cloud.deploy.KubernetesService.INSTANCE_LABEL;
import static dev.streamx.cli.command.cloud.deploy.KubernetesService.MANAGED_BY_LABEL;
import static dev.streamx.cli.command.cloud.deploy.KubernetesService.MANAGED_BY_LABEL_VALUE;
import static dev.streamx.cli.command.cloud.deploy.KubernetesService.NAME_LABEL;
import static dev.streamx.cli.command.cloud.deploy.KubernetesService.PART_OF_LABEL;
import static org.assertj.core.api.Assertions.assertThat;

import dev.streamx.cli.command.cloud.deploy.Config.ConfigType;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Secret;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.Map;
import org.junit.jupiter.api.Test;

@QuarkusTest
class KubernetesServiceTest {

  private static final String MESH_NAME = "sx";
  @Inject
  KubernetesService cut;

  @Test
  void shouldReturnConfigMapWithRequiredMetadataAndData() {
    Map<String, String> data = Map.of("key", "value");
    ConfigType configType = ConfigType.FILE;
    Config config = new Config("test/default.conf", data, configType);
    ConfigMap configMap = cut.buildConfigMap(MESH_NAME, config);
    ObjectMeta metadata = configMap.getMetadata();
    assertThat(metadata.getName()).isEqualTo("sx-extcfg-test-default-conf");
    Map<String, String> expectedLabels = Map.of(
        NAME_LABEL, "test-default-conf",
        INSTANCE_LABEL, "sx-extcfg-test-default-conf",
        COMPONENT_LABEL, COMPONENT_EXTERNAL_CONFIG,
        MANAGED_BY_LABEL, MANAGED_BY_LABEL_VALUE,
        CONFIG_TYPE_LABEL, configType.getLabelValue(),
        PART_OF_LABEL, "sx"
    );
    assertThat(metadata.getLabels()).containsExactlyInAnyOrderEntriesOf(expectedLabels);
    assertThat(configMap.getData()).containsExactlyInAnyOrderEntriesOf(data);
  }

  @Test
  void shouldReturnSecretWithRequiredMetadataAndData() {
    ConfigType configType = ConfigType.DIR;
    Map<String, String> data = Map.of("key", "value");
    Config config = new Config("test/default.conf", data, configType);
    Secret secret = cut.buildSecret(MESH_NAME, config);
    ObjectMeta metadata = secret.getMetadata();
    assertThat(metadata.getName()).isEqualTo("sx-extsec-test-default-conf");
    Map<String, String> expectedLabels = Map.of(
        NAME_LABEL, "test-default-conf",
        INSTANCE_LABEL, "sx-extsec-test-default-conf",
        COMPONENT_LABEL, COMPONENT_EXTERNAL_SECRET,
        MANAGED_BY_LABEL, MANAGED_BY_LABEL_VALUE,
        CONFIG_TYPE_LABEL, configType.getLabelValue(),
        PART_OF_LABEL, "sx"
    );
    assertThat(metadata.getLabels()).containsExactlyInAnyOrderEntriesOf(expectedLabels);
    assertThat(secret.getStringData()).containsExactlyInAnyOrderEntriesOf(data);
  }
}