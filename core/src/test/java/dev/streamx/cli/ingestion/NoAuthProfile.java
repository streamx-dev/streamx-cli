package dev.streamx.cli.ingestion;

import io.quarkus.test.junit.QuarkusTestProfile;
import java.util.Map;

public class NoAuthProfile implements QuarkusTestProfile {

  @Override
  public Map<String, String> getConfigOverrides() {
    return Map.of("streamx.auth-token", "");
  }
}
