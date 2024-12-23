package dev.streamx.cli.command.cloud.deploy;

import io.quarkus.test.junit.QuarkusTestProfile;
import java.util.Map;

public class KubernetesClientProfile implements QuarkusTestProfile {

  @Override
  public Map<String, String> getConfigOverrides() {
    return Map.of("%test.quarkus.kubernetes-client.devservices.enabled", "true");
  }
}
