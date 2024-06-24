package dev.streamx.cli.license;

import static dev.streamx.cli.license.LicenseConfig.STREAMX_ACCEPT_LICENSE;

import dev.streamx.cli.license.LicenseWiremockConfigs.StandardWiremockLicense;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import io.quarkus.test.junit.QuarkusTestProfile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LicenseTestProfiles {

  public static class ProceedingTestProfile implements QuarkusTestProfile {

    public static final String TEST_SETTINGS_PATH_ROOT =
        "target/test-classes/dev.streamx.cli.license/license-settings";

    @Override
    public List<TestResourceEntry> testResources() {
      List<TestResourceEntry> testResourceEntries =
          new ArrayList<>(QuarkusTestProfile.super.testResources());

      testResourceEntries.add(new TestResourceEntry(TestSettingsLifecycleManager.class));
      testResourceEntries.add(new TestResourceEntry(StandardWiremockLicense.class));

      return testResourceEntries;
    }

    @Override
    public Map<String, String> getConfigOverrides() {
      return new HashMap<>(
          Map.of(
          "streamx.cli.license.proceeding.enabled", "true"
          )
      );
    }

    public static class TestSettingsLifecycleManager
        implements QuarkusTestResourceLifecycleManager {

      @Override
      public Map<String, String> start() {
        return Map.of("streamx.cli.settings.root-dir", TEST_SETTINGS_PATH_ROOT);
      }

      @Override
      public void stop() {

      }
    }
  }

  public static class AcceptProceedingTestProfile extends ProceedingTestProfile {

    @Override
    public Map<String, String> getConfigOverrides() {
      Map<String, String> result = new HashMap<>(super.getConfigOverrides());
      result.put(STREAMX_ACCEPT_LICENSE, "true");
      return result;
    }
  }
}
