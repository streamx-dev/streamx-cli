package dev.streamx.cli.command.cloud;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.streamx.cli.command.cloud.deploy.Config;
import dev.streamx.cli.exception.KubernetesException;
import dev.streamx.cli.interpolation.Interpolating;
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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
  private static final String DEFAULT_K8S_NAMESPACE = "default";
  @Inject
  KubernetesClient kubernetesClient;
  @Inject
  KubernetesConfig kubernetesConfig;
  @Inject
  @Interpolating
  ObjectMapper objectMapper;

  private static void setMetadata(String meshName, Component component, String name,
      HasMetadata resource) {
    String instanceName = getResourceName(meshName, component.getShortName(), name);
    resource.getMetadata().setName(instanceName);
    setLabel(resource, INSTANCE_LABEL, instanceName);
    setLabel(resource, COMPONENT_LABEL, component.getName());
    setLabel(resource, NAME_LABEL, name);
    setManagedByAndPartOfLabels(resource, meshName);
  }

  private static void setManagedByAndPartOfLabels(HasMetadata resource, String meshName) {
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

  public <T extends HasMetadata> void undeploy(List<T> resources) {
    resources.forEach(this::undeploy);
  }

  public void undeploy(HasMetadata resource) {
    try {
      kubernetesClient.resource(resource)
          .inNamespace(getNamespace()).delete();
    } catch (KubernetesClientException e) {
      throw KubernetesException.kubernetesClientException(e);
    }
  }

  public void undeploy(String meshName) {
    try {
      Map<String, String> selector = createPartOfAndManagedByLabels(meshName);
      kubernetesClient.resources(ServiceMesh.class).inNamespace(getNamespace())
          .withName(meshName).delete();
      kubernetesClient.resources(ConfigMap.class).inNamespace(getNamespace())
          .withLabels(selector).delete();
      kubernetesClient.resources(Secret.class).inNamespace(getNamespace())
          .withLabels(selector).delete();

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
    return kubernetesConfig.namespace()
        .orElse(Optional.ofNullable(kubernetesClient.getNamespace()).orElse(DEFAULT_K8S_NAMESPACE));
  }

  public List<String> getResourcesPaths() {
    return kubernetesConfig.resourcesDirectories().map(paths -> Arrays.stream(paths.split(","))
        .map(String::trim)
        .collect(Collectors.toList())).orElse(List.of());
  }

  public List<HasMetadata> buildResourcesFromDirectory(String resourcesDirectory, Path projectPath,
      String meshName) {
    Path dirPath = projectPath.resolve(resourcesDirectory);

    if (!Files.exists(dirPath) || !Files.isDirectory(dirPath)) {
      throw new RuntimeException(
          "Kubernetes resources directory: %s does not exist".formatted(dirPath));
    }

    List<HasMetadata> resources = new ArrayList<>();

    try (Stream<Path> files = Files.list(dirPath)) {
      files.forEach(file -> processResourceFile(file, resources));
    } catch (IOException e) {
      throw new RuntimeException("Error reading directory: " + dirPath, e);
    }

    resources.forEach(r -> setManagedByAndPartOfLabels(r, meshName));
    return resources;
  }

  private void processResourceFile(Path file, List<HasMetadata> resources) {
    String fileName = file.getFileName().toString();
    if (!fileName.endsWith("yaml") && !fileName.endsWith("yml")) {
      return;
    }
    try (MappingIterator<HasMetadata> iterator =
        this.objectMapper.readerFor(HasMetadata.class).readValues(file.toFile())) {

      while (iterator.hasNext()) {
        resources.add(iterator.next());
      }

    } catch (IOException e) {
      throw new RuntimeException("Failed to load resource from file: " + file, e);
    }
  }

  private Map<String, String> createPartOfAndManagedByLabels(String meshName) {
    return Map.of(
        PART_OF_LABEL, meshName,
        MANAGED_BY_LABEL, MANAGED_BY_LABEL_VALUE
    );
  }
}
