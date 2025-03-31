package dev.streamx.cli.command.cloud;

import static dev.streamx.cli.command.cloud.MetadataUtils.CONFIG_TYPE_LABEL;
import static dev.streamx.cli.command.cloud.MetadataUtils.DEFAULT_K8S_NAMESPACE;
import static dev.streamx.cli.command.cloud.MetadataUtils.SERVICEMESH_CRD_NAME;
import static dev.streamx.cli.command.cloud.MetadataUtils.setLabel;
import static dev.streamx.cli.command.cloud.MetadataUtils.setMetadata;

import dev.streamx.cli.command.cloud.collector.ClusterResourcesCollector;
import dev.streamx.cli.command.cloud.collector.TypedClusterResourceCollector;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

@ApplicationScoped
public class KubernetesService {

  @Inject
  KubernetesClient kubernetesClient;
  @Inject
  KubernetesConfig kubernetesConfig;

  public <T extends HasMetadata> void deploy(List<T> resources) {
    resources.forEach(this::deploy);
  }

  private <T extends HasMetadata> void deploy(T resource) {
    try {
      kubernetesClient.resource(resource).inNamespace(getNamespace())
          .createOr(NonDeletingOperation::update);
    } catch (KubernetesClientException e) {
      throw KubernetesException.kubernetesClientException(e);
    }
  }

  public void undeploy(String meshName) {
    undeploy(collectManagedResources(meshName));
  }

  public void undeploy(List<HasMetadata> resources) {
    try {
      resources.forEach(r -> kubernetesClient.resource(r).delete());
    } catch (KubernetesClientException e) {
      throw KubernetesException.kubernetesClientException(e);
    }
  }

  public List<HasMetadata> collectManagedResources(String meshName) {
    List<HasMetadata> result = new ArrayList<>();
    // Collect mesh
    ServiceMesh mesh = kubernetesClient.resources(ServiceMesh.class).inNamespace(getNamespace())
        .withName(meshName).get();
    if (mesh != null) {
      result.add(mesh);
    }

    // Collect configs and secrets
    result.addAll(
        new TypedClusterResourceCollector(kubernetesClient, List.of(ConfigMap.class, Secret.class),
            getNamespace()).collect(meshName));

    // Collect other resources controlled by the CLI
    result.addAll(new ClusterResourcesCollector(kubernetesClient,
        getControlledResourceDefinitions(), getNamespace()).collect(meshName));

    return result;
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
    return kubernetesConfig.namespace()
        .orElse(Optional.ofNullable(kubernetesClient.getNamespace()).orElse(DEFAULT_K8S_NAMESPACE));
  }

  public List<String> getResourcePaths() {
    return kubernetesConfig.resourceDirectories().map(paths -> Arrays.stream(paths.split(","))
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .collect(Collectors.toList())).orElse(List.of());
  }

  public List<String> getControlledResourceDefinitions() {
    return kubernetesConfig.controlledResourceDefinitions()
        .map(paths -> Arrays.stream(paths.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toList())).orElse(List.of());
  }
}
