package dev.streamx.local.it.github;

import static org.junit.jupiter.api.Assertions.assertTrue;

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
@TestProfile(NotIndexableResourceToLocalStreamXPublishTestIT.SimpleNamedActionTestProfile.class)
public class NotIndexableResourceToLocalStreamXPublishTestIT {

  @Test
  @Launch(value = {})
  public void testLaunchCommand(LaunchResult result) {
    System.out.println(result.getOutput());
    assertTrue(result.getOutput().contains("'pages/not-indexable-page.html' was sent successfully"));
    // open local index http://web.127.0.0.1.nip.io/search/query and verify that entry is not present
  }


  public static class SimpleNamedActionTestProfile implements QuarkusTestProfile {

    @Override
    public Set<Class<?>> getEnabledAlternatives() {
      return Set.of(MockInputsInitializer.class, MockContextInitializer.class);
    }
  }

  @Alternative
  @Singleton
  public static class MockInputsInitializer implements InputsInitializer {

    @Override
    public Inputs createInputs() {
      return new DefaultTestInputs(Map.of(
          Inputs.ACTION, "publish",
          Constants.STREAMX_INGESTION_URL, "http://ingestion.127.0.0.1.nip.io",
          Constants.INGESTION_INCLUDE_PATTERNS, "[\"pages/not-indexable-page.html\"]",
          Constants.INGESTION_CHANNEL, "pages",
          Constants.INGESTION_TYPE, "page/blog",
          Constants.INGESTION_INDEXABLE, "false",
          Constants.INGESTION_SOURCE_PROVIDER, "BatchSourceProvider"
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
