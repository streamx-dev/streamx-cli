package dev.streamx.cli.command.cloud;

import io.fabric8.kubernetes.api.model.HasMetadata;
import java.util.HashMap;
import java.util.Map;

public class MetadataUtils {
  public static final String MANAGED_BY_LABEL = "app.kubernetes.io/managed-by";
  public static final String MANAGED_BY_LABEL_VALUE = "streamx-cli";
  public static final String PART_OF_LABEL = "app.kubernetes.io/part-of";
  public static final String SERVICEMESH_CRD_NAME = "servicemeshes.streamx.dev";

  private MetadataUtils() {
    // No instances
  }

  public static Map<String, String> createPartOfAndManagedByLabels(String meshName) {
    return Map.of(
        PART_OF_LABEL, meshName,
        MANAGED_BY_LABEL, MANAGED_BY_LABEL_VALUE
    );
  }

  public static void setManagedByAndPartOfLabels(HasMetadata resource, String meshName) {
    setLabel(resource, PART_OF_LABEL, meshName);
    setLabel(resource, MANAGED_BY_LABEL, MANAGED_BY_LABEL_VALUE);
  }

  public static void setLabel(HasMetadata resource, String key, String value) {
    if (resource.getMetadata().getLabels() == null) {
      resource.getMetadata().setLabels(new HashMap<>());
    }
    resource.getMetadata().getLabels().put(key, value);
  }
}
