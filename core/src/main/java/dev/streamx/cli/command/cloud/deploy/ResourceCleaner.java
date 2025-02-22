package dev.streamx.cli.command.cloud.deploy;

import io.fabric8.kubernetes.api.model.HasMetadata;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ResourceCleaner {

  private final List<HasMetadata> resourcesToDeploy;
  private final List<HasMetadata> managedResources;

  /**
   * Constructor takes two lists:
   *
   * @param resourcesToDeploy list of resources planned for deployment
   * @param managedResources  list of currently managed resources
   */
  public ResourceCleaner(List<HasMetadata> resourcesToDeploy, List<HasMetadata> managedResources) {
    this.resourcesToDeploy = resourcesToDeploy;
    this.managedResources = managedResources;
  }

  /**
   * Returns the list of orphaned managed resources, i.e. those managed resources that are not
   * present in the deployment list, based on namespace and name.
   *
   * @return List of resources to remove.
   */
  public List<HasMetadata> getOrphanedResources() {
    // Build a set of keys (namespace/name) for resources to deploy.
    Set<String> deployKeys = resourcesToDeploy.stream()
        .map(this::keyFor)
        .collect(Collectors.toSet());

    // Filter managedResources to keep only those not present in deployKeys.
    return managedResources.stream()
        .filter(resource -> !deployKeys.contains(keyFor(resource)))
        .collect(Collectors.toList());
  }

  private String keyFor(HasMetadata resource) {
    String api = resource.getApiVersion() + "/" + resource.getKind() + "/";
    String name = resource.getMetadata().getName();
    return api + "/" + name;
  }
}