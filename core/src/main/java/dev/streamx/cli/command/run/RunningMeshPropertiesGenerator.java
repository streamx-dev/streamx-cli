package dev.streamx.cli.command.run;

import static dev.streamx.cli.command.ingestion.IngestionClientConfig.STREAMX_INGESTION_AUTH_TOKEN;

import dev.streamx.cli.config.DotStreamxGeneratedConfigSource;
import dev.streamx.runner.mesh.MeshContext;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class RunningMeshPropertiesGenerator {

  private RunningMeshPropertiesGenerator() {
    // no instance
  }

  public static void generateRootAuthToken(MeshContext context) {
    Map<String, String> tokensBySource = context.getTokensBySource();
    if (tokensBySource != null) {
      InputStream input = null;
      OutputStream output = null;
      try {
        String streamxConfigPath = DotStreamxGeneratedConfigSource.getUrl().getPath();
        input = new FileInputStream(streamxConfigPath);

        Properties properties = new Properties();
        properties.load(input);
        properties.setProperty(STREAMX_INGESTION_AUTH_TOKEN, tokensBySource.get("root"));

        output = new FileOutputStream(streamxConfigPath);
        properties.store(output, null);
        FileUtils.forceDeleteOnExit(DotStreamxGeneratedConfigSource.getConfigDir().toFile());
      } catch (IOException e) {
        throw new RuntimeException("Failed to setup root authentication token", e);
      } finally {
        IOUtils.closeQuietly(input);
        IOUtils.closeQuietly(output);
      }
    }
  }
}
