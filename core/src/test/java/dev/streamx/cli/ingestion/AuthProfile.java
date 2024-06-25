package dev.streamx.cli.ingestion;

import io.quarkus.test.junit.QuarkusTestProfile;
import java.util.Map;

public class AuthProfile implements QuarkusTestProfile {

  public static final String JWT_TOKEN = "JWT_TOKEN";

  @Override
  public Map<String, String> getConfigOverrides() {
    return Map.of("streamx.ingestion.auth-token", JWT_TOKEN);
  }
}
