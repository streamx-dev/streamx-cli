package dev.streamx.cli.command.cloud;

import static dev.streamx.cli.command.cloud.ServiceMeshService.SERVICE_MESH_NAME;

import dev.streamx.cli.command.cloud.deploy.Config;
import dev.streamx.cli.exception.KubernetesException;
import dev.streamx.operator.Component;
import dev.streamx.operator.crd.ServiceMesh;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinition;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.dsl.NonDeletingOperation;
import io.fabric8.kubernetes.client.utils.KubernetesResourceUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

@ApplicationScoped
public class KubernetesService {

  public static final String NAME_LABEL = "app.kubernetes.io/name";
  public static final String INSTANCE_LABEL = "app.kubernetes.io/instance";
  public static final String COMPONENT_LABEL = "app.kubernetes.io/component";
  public static final String MANAGED_BY_LABEL = "app.kubernetes.io/managed-by";
  public static final String MANAGED_BY_LABEL_VALUE = "streamx-cli";
  public static final String CONFIG_TYPE_LABEL = "mesh.streamx.dev/config-type";
  public static final String PART_OF_LABEL = "app.kubernetes.io/part-of";
  public static final String SERVICEMESH_CRD_NAME = "servicemeshes.streamx.dev";
  private static final Map<String, String> CONFIG_SELECTOR_LABELS = Map.of(
      PART_OF_LABEL, SERVICE_MESH_NAME,
      MANAGED_BY_LABEL, MANAGED_BY_LABEL_VALUE
  );
  @Inject
  KubernetesClient kubernetesClient;
  @Inject
  KubernetesConfig kubernetesConfig;

  private static void setMetadata(String meshName, Component component, String name,
      HasMetadata resource) {
    String instanceName = getResourceName(meshName, component.getShortName(), name);
    resource.getMetadata().setName(instanceName);
    setLabel(resource, INSTANCE_LABEL, instanceName);
    setLabel(resource, COMPONENT_LABEL, component.getName());
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

  public <T extends HasMetadata> void deploy(List<T> resources) {
    resources.forEach(this::deploy);
  }

  public <T extends HasMetadata> void deploy(T resource) {
    try {
      kubernetesClient.resource(resource).inNamespace(getNamespace())
          .createOr(NonDeletingOperation::update);
    } catch (KubernetesClientException e) {
      throw KubernetesException.kubernetesClientException(e);
    }
  }

  public void undeploy() {
    try {
      kubernetesClient.resources(ServiceMesh.class).inNamespace(getNamespace())
          .withName(SERVICE_MESH_NAME).delete();
      kubernetesClient.resources(ConfigMap.class).withLabels(CONFIG_SELECTOR_LABELS).delete();
      kubernetesClient.resources(Secret.class).withLabels(CONFIG_SELECTOR_LABELS).delete();
    } catch (KubernetesClientException e) {
      throw KubernetesException.kubernetesClientException(e);
    }
  }

  public void validateCrdInstallation() {
    try {
      CustomResourceDefinition crd = kubernetesClient.apiextensions().v1()
          .customResourceDefinitions()
          .withName(SERVICEMESH_CRD_NAME)
          .get();
      if (crd == null) {
        throw KubernetesException.serviceMeshCrdNotFound();
      }
    } catch (KubernetesClientException e) {
      throw KubernetesException.kubernetesClientException(e);
    }
  }

  @NotNull
  public ConfigMap buildConfigMap(String meshName, Config config) {
    ConfigMap configMap = new ConfigMapBuilder()
        .withNewMetadata()
        .endMetadata()
        .addToData(config.data())
        .build();
    String sanitizedName = KubernetesResourceUtil.sanitizeName(config.name());
    setMetadata(meshName, Component.EXTERNAL_CONFIG, sanitizedName, configMap);
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
    setMetadata(meshName, Component.EXTERNAL_SECRET, sanitizedName, secret);
    setLabel(secret, CONFIG_TYPE_LABEL, config.configType().getLabelValue());
    return secret;
  }

  public String getNamespace() {
    return kubernetesConfig.namespace().orElse(kubernetesClient.getNamespace());
  }

}
