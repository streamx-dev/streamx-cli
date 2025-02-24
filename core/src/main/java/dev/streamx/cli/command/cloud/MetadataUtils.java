package dev.streamx.cli.command.cloud;

import dev.streamx.operator.Component;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.utils.KubernetesResourceUtil;
import java.util.HashMap;
import java.util.Map;

public class MetadataUtils {

  public static final String NAME_LABEL = "app.kubernetes.io/name";
  public static final String INSTANCE_LABEL = "app.kubernetes.io/instance";
  public static final String COMPONENT_LABEL = "app.kubernetes.io/component";
  public static final String MANAGED_BY_LABEL = "app.kubernetes.io/managed-by";
  public static final String MANAGED_BY_LABEL_VALUE = "streamx-cli";
  public static final String CONFIG_TYPE_LABEL = "mesh.streamx.dev/config-type";
  public static final String PART_OF_LABEL = "app.kubernetes.io/part-of";
  public static final String SERVICEMESH_CRD_NAME = "servicemeshes.streamx.dev";
  public static final String DEFAULT_K8S_NAMESPACE = "default";

  private MetadataUtils() {
    // No instances
  }

  public static Map<String, String> createPartOfAndManagedByLabels(String meshName) {
    return Map.of(
        PART_OF_LABEL, meshName,
        MANAGED_BY_LABEL, MANAGED_BY_LABEL_VALUE
    );
  }

  public static void setMetadata(String meshName, Component component, String name,
      HasMetadata resource) {
    String instanceName = getResourceName(meshName, component.getShortName(), name);
    resource.getMetadata().setName(instanceName);
    setLabel(resource, INSTANCE_LABEL, instanceName);
    setLabel(resource, COMPONENT_LABEL, component.getName());
    setLabel(resource, NAME_LABEL, name);
    setManagedByAndPartOfLabels(resource, meshName);
  }

  public static void setManagedByAndPartOfLabels(HasMetadata resource, String meshName) {
    setLabel(resource, PART_OF_LABEL, meshName);
    setLabel(resource, MANAGED_BY_LABEL, MANAGED_BY_LABEL_VALUE);
  }

  public static String getResourceName(String meshName, String componentName, String name) {
    return KubernetesResourceUtil.sanitizeName(meshName + "-" + componentName + "-" + name);
  }

  public static void setLabel(HasMetadata resource, String key, String value) {
    if (resource.getMetadata().getLabels() == null) {
      resource.getMetadata().setLabels(new HashMap<>());
    }
    resource.getMetadata().getLabels().put(key, value);
  }
}
