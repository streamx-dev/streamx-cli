package dev.streamx.cli.command.cloud.undeploy;

import static dev.streamx.cli.util.Output.printf;
import static org.apache.commons.lang3.StringUtils.removeStart;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.base.ResourceDefinitionContext;
import java.util.List;
import java.util.Map;

public class ResourceDeleter {

  private static String CLUSTER_RESOURCE_TYPE_MARKER = "cluster:";

  private final KubernetesClient kubernetesClient;
  private final Map<String, String> selector;
  private final String namespace;

  public ResourceDeleter(KubernetesClient kubernetesClient, Map<String, String> selector,
      String namespace) {
    this.kubernetesClient = kubernetesClient;
    this.selector = selector;
    this.namespace = namespace;
  }

  private String getNamespace() {
    return namespace;
  }

  public void deleteResourcesFromProperties(List<String> resourceDefinitions) {
    // Split the property string by comma
    resourceDefinitions.forEach(entry -> {
      boolean clusterScoped = entry.startsWith(CLUSTER_RESOURCE_TYPE_MARKER);
      String[] parts = (clusterScoped ? removeStart(entry, CLUSTER_RESOURCE_TYPE_MARKER)
          : entry).split("/");

      if (parts.length != 3 && parts.length != 2) {
        throw new IllegalArgumentException("Invalid resource specification: " + entry
            + ". Expected format: group/version/kind or version/kind");
      }
      // For resources like Pod or ConfigMap, the group is skipped
      boolean isCoreResource = parts.length == 2;
      String group = isCoreResource ? "" : parts[0].trim();
      String version = isCoreResource ? parts[0].trim() : parts[1].trim();
      String kind = isCoreResource ? parts[1].trim() : parts[2].trim();

      int result = deleteResourcesByType(group, version, kind, clusterScoped);
      printf("Deleted %d controlled resources of type '%s/%s/%s'\n", result, group, version, kind);
    });
  }

  /**
   * Deletes resources of the given API group, version, and kind, using the global label selector.
   */
  public int deleteResourcesByType(String group, String version, String kind,
      boolean clusterScoped) {
    ResourceDefinitionContext context = new ResourceDefinitionContext.Builder()
        .withGroup(group)
        .withVersion(version)
        .withKind(kind)
        .withNamespaced(!clusterScoped)
        .build();

    // Delete all resources matching the selector in the given namespace.
    return kubernetesClient.genericKubernetesResources(context)
        .inNamespace(getNamespace())
        .withLabels(selector)
        .delete().size();
  }
}
