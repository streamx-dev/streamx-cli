package dev.streamx.cli.ingestion;

import io.quarkus.test.junit.QuarkusTestProfile;
import java.util.Map;

public class AuthorizedProfile implements QuarkusTestProfile {

  public static final String AUTH_TOKEN = "AUTH_TOKEN";

  @Override
  public Map<String, String> getConfigOverrides() {
    return Map.of("streamx.ingestion.auth-token", AUTH_TOKEN);
  }
}
