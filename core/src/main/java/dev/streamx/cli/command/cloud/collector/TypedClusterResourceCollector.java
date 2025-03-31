package dev.streamx.cli.command.cloud.collector;

import static dev.streamx.cli.command.cloud.MetadataUtils.createPartOfAndManagedByLabels;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.KubernetesClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Collects controlled resources deployed to the Kubernetes cluster. Identifies the resources by the
 * assigned labels. The scope of the class is the mesh namespace and the resource classes.
 */
public class TypedClusterResourceCollector implements KubernetesResourcesCollector {

  private final KubernetesClient kubernetesClient;
  private final List<Class<? extends HasMetadata>> resourceDefinitions;
  private final String namespace;

  public TypedClusterResourceCollector(KubernetesClient kubernetesClient,
      List<Class<? extends HasMetadata>> resourceDefinitions,
      String namespace) {
    this.kubernetesClient = kubernetesClient;
    this.resourceDefinitions = resourceDefinitions;
    this.namespace = namespace;
  }

  @Override
  public List<HasMetadata> collect(String meshName) {
    Map<String, String> selector = createPartOfAndManagedByLabels(meshName);
    List<HasMetadata> result = new ArrayList<>();
    resourceDefinitions.forEach(r ->
        result.addAll(kubernetesClient.resources(r).inNamespace(namespace)
            .withLabels(selector).list().getItems())
    );
    return result;
  }
}
