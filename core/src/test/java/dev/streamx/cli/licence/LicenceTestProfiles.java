package dev.streamx.cli.licence;

import dev.streamx.cli.licence.LicenceWiremockConfigs.StandardWiremockLicence;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import io.quarkus.test.junit.QuarkusTestProfile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LicenceTestProfiles {

  public static class AcceptingLicenceTestProfile extends ProceedingTestProfile {

  }

  public static class RejectingLicenceTestProfile extends ProceedingTestProfile {

    @Override
    public Map<String, String> getConfigOverrides() {
      Map<String, String> overrides = super.getConfigOverrides();
      overrides.put("streamx.cli.licence.accepting-strategy.fixed.value", "false");
      return overrides;
    }
  }

  public static class ProceedingTestProfile implements QuarkusTestProfile {

    public static final String TEST_SETTINGS_PATH_ROOT =
        "target/test-classes/dev.streamx.cli.license/license-settings";

    @Override
    public List<TestResourceEntry> testResources() {
      List<TestResourceEntry> testResourceEntries =
          new ArrayList<>(QuarkusTestProfile.super.testResources());

      testResourceEntries.add(new TestResourceEntry(TestSettingsLifecycleManager.class));
      testResourceEntries.add(new TestResourceEntry(StandardWiremockLicence.class));

      return testResourceEntries;
    }

    @Override
    public Map<String, String> getConfigOverrides() {
      return new HashMap<>(
          Map.of(
          "streamx.cli.licence.strategy", "PROCEED"
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
}
