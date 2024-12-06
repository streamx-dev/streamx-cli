package dev.streamx.cli.command.ingestion;

import static dev.streamx.cli.command.ingestion.IngestionClientConfig.STREAMX_INGESTION_AUTH_TOKEN;

import io.quarkus.test.junit.QuarkusTestProfile;
import java.util.Map;

public class AuthorizedProfile implements QuarkusTestProfile {

  public static final String AUTH_TOKEN = "AUTH_TOKEN";

  @Override
  public Map<String, String> getConfigOverrides() {
    return Map.of(STREAMX_INGESTION_AUTH_TOKEN, AUTH_TOKEN);
  }
}
