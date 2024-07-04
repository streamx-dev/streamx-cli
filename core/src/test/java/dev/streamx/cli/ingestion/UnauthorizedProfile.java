package dev.streamx.cli.ingestion;

import static dev.streamx.cli.ingestion.IngestionClientConfig.STREAMX_INGESTION_AUTH_TOKEN;

import io.quarkus.test.junit.QuarkusTestProfile;
import java.util.Map;

public class UnauthorizedProfile implements QuarkusTestProfile {

  @Override
  public Map<String, String> getConfigOverrides() {
    return Map.of(STREAMX_INGESTION_AUTH_TOKEN, "");
  }
}
