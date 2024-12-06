package dev.streamx.cli.command.ingestion;

import static dev.streamx.cli.command.ingestion.IngestionClientConfig.STREAMX_INGESTION_AUTH_TOKEN;

import io.quarkus.test.junit.QuarkusTestProfile;
import java.util.Map;

public class UnauthorizedProfile implements QuarkusTestProfile {

  @Override
  public Map<String, String> getConfigOverrides() {
    return Map.of(STREAMX_INGESTION_AUTH_TOKEN, "");
  }
}
