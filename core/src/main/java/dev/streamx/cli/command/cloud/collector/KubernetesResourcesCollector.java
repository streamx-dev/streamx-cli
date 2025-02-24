package dev.streamx.cli.command.cloud.collector;

import io.fabric8.kubernetes.api.model.HasMetadata;
import java.util.List;

public interface KubernetesResourcesCollector {

  List<HasMetadata> collect(String meshName);

}
