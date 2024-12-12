package dev.streamx.cli.command.manage;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.dockerjava.api.DockerClient;
import dev.streamx.cli.command.manage.event.MeshManagerStarted;
import io.quarkus.runtime.ApplicationLifecycleManager;
import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainLauncher;
import io.quarkus.test.junit.main.QuarkusMainTest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.shaded.com.github.dockerjava.core.command.ExecStartResultCallback;
import org.testcontainers.shaded.org.apache.commons.io.IOUtils;


@QuarkusMainTest
class ManageCommandTest {

  public static final String HOST_DIRECTORY = "target/test-classes";
  public static final String HOST_MESH_PATH = HOST_DIRECTORY + "/mesh.yaml";

  @Test
  void shouldManageExampleMesh(QuarkusMainLauncher launcher) {
    // given
    var meshPath = Paths.get(HOST_MESH_PATH);
    String s = meshPath
        .normalize()
        .toAbsolutePath()
        .toString();

    // when
    LaunchResult result = launcher.launch("manage", "-f=" + s);

    // then
    var errorOutput = getErrorOutput(result);

    assertThat(errorOutput).doesNotContain(StateVerifier.MESH_CONTENT_DIFFERENT);
    assertThat(errorOutput).doesNotContain(StateVerifier.PROJECT_DIR_DIFFERENT);
    assertThat(errorOutput).isBlank();

    assertThat(result.exitCode()).isZero();
    assertThat(result.getOutput()).contains("StreamX Mesh Manager started on");
  }

  @NotNull
  private static String getErrorOutput(LaunchResult result) {
    var errorOutput = result.getErrorOutput();
    if (!errorOutput.isBlank()) {
      System.err.println(errorOutput);
    }
    return errorOutput;
  }

  @ApplicationScoped
  public static class StateVerifier {
    public static final String PROJECT_DIR_DIFFERENT =
        "Provided project directory contend is different than container project.";
    public static final String MESH_CONTENT_DIFFERENT =
        "Provided mesh and container mesh have different content.";

    private final DockerClient client = DockerClientFactory.instance().client();

    void onMeshStarted(@Observes MeshManagerStarted event) throws Exception {
      compareMeshContent();
      compareProjectDirectoryContent();

      ApplicationLifecycleManager.exit();
    }

    private void compareProjectDirectoryContent() throws InterruptedException {
      var command = "ls -1 /data/project";

      byte[] containerMeshContent = executeCommand(client, command);

      var containerDirectoryContent = extractContainerFiles(containerMeshContent);
      var directoryContent = extractHostFiles();

      if (!containerDirectoryContent.equals(directoryContent)) {
        System.err.println(PROJECT_DIR_DIFFERENT);
      }
    }

    @NotNull
    private static Set<String> extractHostFiles() {
      return Arrays.stream(new File(HOST_DIRECTORY).listFiles())
          .map(File::getName)
          .collect(Collectors.toSet());
    }

    @NotNull
    private static Set<String> extractContainerFiles(byte[] containerMeshContent) {
      var lines = IOUtils.readLines(new ByteArrayInputStream(containerMeshContent),
          StandardCharsets.UTF_8);

      return lines.stream()
          .filter(Objects::nonNull)
          .map(String::trim)
          .collect(Collectors.toSet());
    }

    private void compareMeshContent() throws InterruptedException, IOException {
      var command = "cat /data/mesh.yaml";

      byte[] containerMeshContent = executeCommand(client, command);
      var meshPath = Paths.get(HOST_MESH_PATH);

      if (!Arrays.equals(Files.readAllBytes(meshPath), containerMeshContent)) {
        System.err.println(MESH_CONTENT_DIFFERENT);
      }
    }

    private byte[] executeCommand(DockerClient client, String command) throws InterruptedException {
      var execId = client
          .execCreateCmd("streamx-mesh-manager")
          .withCmd("sh", "-c", command)
          .withAttachStdout(true)
          .exec()
          .getId();

      var outputStream = new ByteArrayOutputStream();
      var results = new ExecStartResultCallback(outputStream, null);

      client.execStartCmd(execId)
          .exec(results)
          .awaitCompletion();

      return outputStream.toByteArray();
    }
  }
}
