package dev.streamx.cli.command.cloud.collector;

import static dev.streamx.cli.command.cloud.MetadataUtils.createPartOfAndManagedByLabels;
import static org.apache.commons.lang3.StringUtils.removeStart;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.base.ResourceDefinitionContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Collects controlled resources deployed to the Kubernetes cluster. Identifies the resources by the
 * assigned labels. The scope of the class is the mesh namespace and the list of resources
 * definitions, i.e:
 * <pre>
 *   - v1/Pod
 *   - apps/v1/Deployment
 *   - v1/Service
 *   - batch/v1/Job
 *   - cluster:rbac.authorization.k8s.io/v1/ClusterRole
 * </pre>
 */
public class ClusterResourcesCollector implements KubernetesResourcesCollector {

  private static final String CLUSTER_RESOURCE_TYPE_MARKER = "cluster:";

  private final KubernetesClient kubernetesClient;
  private final List<String> resourceDefinitions;
  private final String namespace;

  public ClusterResourcesCollector(KubernetesClient kubernetesClient,
      List<String> resourceDefinitions,
      String namespace) {
    this.kubernetesClient = kubernetesClient;
    this.resourceDefinitions = resourceDefinitions;
    this.namespace = namespace;
  }

  public List<HasMetadata> collect(String meshName) {

    List<HasMetadata> result = new ArrayList<>();
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

      result.addAll(collectResourcesByType(meshName, group, version, kind, clusterScoped));
    });

    return result;
  }

  /**
   * Collects resources of the given API group, version, and kind, using the global label selector.
   */
  private List<HasMetadata> collectResourcesByType(String meshName, String group, String version,
      String kind, boolean clusterScoped) {
    Map<String, String> selector = createPartOfAndManagedByLabels(meshName);

    ResourceDefinitionContext context = new ResourceDefinitionContext.Builder()
        .withGroup(group)
        .withVersion(version)
        .withKind(kind)
        .withNamespaced(!clusterScoped)
        .build();

    return new ArrayList<>(kubernetesClient.genericKubernetesResources(context)
        .inNamespace(namespace)
        .withLabels(selector).list().getItems());
  }
}
