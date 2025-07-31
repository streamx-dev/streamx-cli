package dev.streamx.githhub.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkiverse.githubaction.Context;
import io.quarkiverse.githubaction.Inputs;
import java.io.File;
import java.nio.file.Path;
import org.junit.jupiter.api.Disabled;
import org.kohsuke.github.GHEventPayload;
import org.mockito.Mock;

@Disabled
public class AbstractSourceProviderTest {

  @Mock
  protected Inputs inputs;
  @Mock
  protected Context context;
  @Mock
  protected GHEventPayload payload;

  protected ObjectMapper objectMapper = new ObjectMapper();

  protected Path getTestWorkspacePath() {
    File testWorkspace = new File("src/test/resources/dev/streamx/github/files");
    return Path.of(testWorkspace.getAbsolutePath());
  }

}
