package dev.streamx.cloud.it.github;

import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.streamx.cloud.it.github.TestWebResourceToCloudTestIT.CloudQuarkusTestProfile;
import dev.streamx.githhub.Constants;
import io.quarkiverse.githubaction.Context;
import io.quarkiverse.githubaction.ContextInitializer;
import io.quarkiverse.githubaction.Inputs;
import io.quarkiverse.githubaction.InputsInitializer;
import io.quarkiverse.githubaction.testing.DefaultTestContext;
import io.quarkiverse.githubaction.testing.DefaultTestInputs;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.junit.main.Launch;
import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainTest;
import jakarta.enterprise.inject.Alternative;
import jakarta.inject.Singleton;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

@QuarkusMainTest
@TestProfile(CloudQuarkusTestProfile.class)
public class TestWebResourceToCloudTestIT {


  @Test
  @Launch(value = {})
  public void testLaunchCommand(LaunchResult result) {
    System.out.println(result.getOutput());
    // open https://arbory--blog--web.edge-ap-southeast-1.streamx.cloud/fonts/roboto-regular.woff2 and verify that entry is present
    assertTrue(result.getOutput().contains("'fonts/roboto-regular.woff2' was sent successfully"));
    assertTrue(result.getOutput().contains("'fonts/roboto-bold.woff2' was sent successfully"));
    assertTrue(
        result.getOutput().contains("'fonts/Poppins-VariableFont.woff2' was sent successfully"));
    assertTrue(result.getOutput().contains("'fonts/Poppins-Regular.woff2' was sent successfully"));
    assertTrue(result.getOutput().contains("'fonts/Poppins-Bold.woff2' was sent successfully"));
    assertTrue(result.getOutput()
        .contains("'fonts/Poppins-Italic-VariableFont.woff2' was sent successfully"));
  }

  public static class CloudQuarkusTestProfile implements QuarkusTestProfile {

    @Override
    public Set<Class<?>> getEnabledAlternatives() {
      return Set.of(TestWebResourceToCloudTestIT.MockInputsInitializer.class, TestWebResourceToCloudTestIT.MockContextInitializer.class);
    }
  }

  @Alternative
  @Singleton
  public static class MockInputsInitializer implements InputsInitializer {

    @Override
    public Inputs createInputs() {
      return new DefaultTestInputs(Map.of(
          Inputs.ACTION, "publish",
          Constants.INGESTION_INCLUDE_PATTERNS, "[\"fonts/*.woff2\"]",
          Constants.INGESTION_CHANNEL, "web-resources",
          Constants.INGESTION_TYPE, "web-resource/static",
          Constants.INGESTION_SOURCE_PROVIDER, "BatchSourceProvider",
          Constants.STREAMX_INGESTION_URL,
          "https://ds-puresight-ingestion.processing-eu-central-1.streamx.cloud",
          Constants.STREAMX_INGESTION_TOKEN, "change_it"
      ));
    }
  }

  @Alternative
  @Singleton
  public static class MockContextInitializer implements ContextInitializer {

    @Override
    public Context createContext() {
      return new DefaultTestContext() {
        @Override
        public String getGitHubWorkspace() {
          Path itResourcesPath = Paths.get("src", "test-it", "resources");
          return itResourcesPath.toAbsolutePath().toString();
        }

      };
    }
  }

}
