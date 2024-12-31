package dev.streamx.cli.command.cloud.deploy;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.NonDeletingOperation;
import io.fabric8.kubernetes.client.utils.KubernetesResourceUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.HashMap;
import java.util.List;
import org.jetbrains.annotations.NotNull;

@ApplicationScoped
public class KubernetesService {

  static final String NAME_LABEL = "app.kubernetes.io/name";
  static final String INSTANCE_LABEL = "app.kubernetes.io/instance";
  static final String COMPONENT_LABEL = "app.kubernetes.io/component";
  static final String MANAGED_BY_LABEL = "app.kubernetes.io/managed-by";
  static final String MANAGED_BY_LABEL_VALUE = "streamx-cli";
  static final String CONFIG_TYPE_LABEL = "mesh.streamx.dev/config-type";
  static final String PART_OF_LABEL = "app.kubernetes.io/part-of";
  static final String COMPONENT_EXTERNAL_CONFIG = "extcfg";
  static final String COMPONENT_EXTERNAL_SECRET = "extsec";

  @Inject
  KubernetesClient kubernetesClient;

  private static void setMetadata(String meshName, String componentName, String name,
      HasMetadata resource) {
    String instanceName = getResourceName(meshName, componentName, name);
    resource.getMetadata().setName(instanceName);
    setLabel(resource, INSTANCE_LABEL, instanceName);
    setLabel(resource, COMPONENT_LABEL, componentName);
    setLabel(resource, NAME_LABEL, name);
    setLabel(resource, PART_OF_LABEL, meshName);
    setLabel(resource, MANAGED_BY_LABEL, MANAGED_BY_LABEL_VALUE);
  }

  private static String getResourceName(String meshName, String componentName, String name) {
    return KubernetesResourceUtil.sanitizeName(meshName + "-" + componentName + "-" + name);
  }

  private static void setLabel(HasMetadata resource, String key, String value) {
    if (resource.getMetadata().getLabels() == null) {
      resource.getMetadata().setLabels(new HashMap<>());
    }
    resource.getMetadata().getLabels().put(key, value);
  }

  public <T extends HasMetadata> void deploy(List<T> resources, String namespace) {
    resources.forEach(r -> deploy(r, namespace));
  }

  public <T extends HasMetadata> void deploy(T resource, String namespace) {
    kubernetesClient.resource(resource).inNamespace(namespace)
        .createOr(NonDeletingOperation::update);
  }

  @NotNull
  public ConfigMap buildConfigMap(String meshName, Config config) {
    ConfigMap configMap = new ConfigMapBuilder()
        .withNewMetadata()
        .endMetadata()
        .addToData(config.data())
        .build();
    String sanitizedName = KubernetesResourceUtil.sanitizeName(config.name());
    setMetadata(meshName, COMPONENT_EXTERNAL_CONFIG, sanitizedName, configMap);
    setLabel(configMap, CONFIG_TYPE_LABEL, config.configType().getLabelValue());
    return configMap;
  }

  @NotNull
  public Secret buildSecret(String meshName, Config config) {
    Secret secret = new SecretBuilder()
        .withNewMetadata()
        .endMetadata()
        .withStringData(config.data())
        .build();
    String sanitizedName = KubernetesResourceUtil.sanitizeName(config.name());
    setMetadata(meshName, COMPONENT_EXTERNAL_SECRET, sanitizedName, secret);
    setLabel(secret, CONFIG_TYPE_LABEL, config.configType().getLabelValue());
    return secret;
  }

}
