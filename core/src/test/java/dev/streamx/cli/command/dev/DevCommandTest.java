package dev.streamx.cli.command.dev;

import static dev.streamx.cli.command.util.MeshTestsUtils.cleanUpMesh;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.dockerjava.api.DockerClient;
import dev.streamx.cli.command.MeshStopper;
import dev.streamx.cli.command.dev.DevCommandTest.DevCommandProfile;
import dev.streamx.cli.command.dev.event.DashboardStarted;
import dev.streamx.cli.command.dev.event.DevReady;
import dev.streamx.runner.event.MeshReloadUpdate;
import dev.streamx.runner.event.MeshStarted;
import io.quarkus.arc.properties.IfBuildProperty;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainLauncher;
import io.quarkus.test.junit.main.QuarkusMainTest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import org.awaitility.Awaitility;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.io.TempDir;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.shaded.com.github.dockerjava.core.command.ExecStartResultCallback;
import org.testcontainers.shaded.org.apache.commons.io.IOUtils;


@QuarkusMainTest
@EnabledIf("dev.streamx.cli.OsUtils#isDockerAvailable")
@TestProfile(DevCommandProfile.class)
class DevCommandTest {

  public static final String MESH_PROPERTY_NAME = "test.mesh.path";
  public static final String HOST_DIRECTORY = "target/test-classes";
  public static final String MESHES_DIRECTORY = HOST_DIRECTORY + "/dev/streamx/cli/command/dev";
  public static final String INITIAL_MESH = MESHES_DIRECTORY + "/initial-mesh.yaml";
  public static final String FAILING_MESH = MESHES_DIRECTORY + "/failing-mesh.yaml";
  public static final String INCREMENTAL_RELOADED_MESH =
      MESHES_DIRECTORY + "/incremental-reloaded-mesh.yaml";

  @TempDir
  public Path temp;

  @AfterEach
  void awaitDockerResourcesAreRemoved() {
    Awaitility.await()
        .until(() -> {
          try {
            Set<String> cleanedUpContainers =
                Set.of("pulsar", "pulsar-init", "streamx-dashboard",
                    "rest-ingestion", "relay", "web-delivery-service");
            cleanUpMesh(cleanedUpContainers);

            return true;
          } catch (Exception e) {
            return false;
          }
        });
  }

  @Test
  void shouldReactOnMeshChanges(QuarkusMainLauncher launcher) {
    // given
    var meshPath = temp.resolve("mesh.yaml");
    String meshPathString = meshPath
        .toAbsolutePath()
        .normalize()
        .toString();

    System.setProperty(MESH_PROPERTY_NAME, meshPathString);

    // when
    LaunchResult result = launcher.launch("dev", "-f=" + meshPathString);

    // then
    var errorOutput = getErrorOutput(result);

    assertThat(errorOutput).doesNotContain(StateVerifier.MESH_CONTENT_DIFFERENT);
    assertThat(errorOutput).doesNotContain(StateVerifier.PROJECT_DIR_DIFFERENT);
    assertThat(errorOutput).isBlank();

    assertThat(result.exitCode()).isZero();
    assertThat(result.getOutput()).contains("StreamX Dashboard started on");
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
  @IfBuildProperty(name = "streamx.dev.test.profile", stringValue = "true")
  public static class StateVerifier {
    private static final String PROJECT_DIR_DIFFERENT =
        "Provided project directory contend is different than container project.";
    private static final String INITIAL_MESH_NOT_EMPTY =
        "Initial mesh file must be empty.";
    private static final String DASHBOARD_NOT_STARTED =
        "Dashboard did not start";
    private static final String MESH_CONTENT_DIFFERENT =
        "Provided mesh and container mesh have different content.";

    private final DockerClient client = DockerClientFactory.instance().client();

    private AtomicBoolean dashboardStarted = new AtomicBoolean(false);

    @Inject
    MeshStopper meshStopper;

    @Inject
    DashboardRunner dashboardRunner;

    void onMeshStarted(@Observes MeshStarted event) throws Exception {
      Path meshPath = getMeshPath();

      Files.copy(Path.of(FAILING_MESH), meshPath, StandardCopyOption.REPLACE_EXISTING);
    }

    void onDevReady(@Observes DevReady event) throws Exception {
      if (!dashboardStarted.get()) {
        System.err.println(DASHBOARD_NOT_STARTED);
      }

      Path meshPath = getMeshPath();

      if (Files.size(meshPath) > 0) {
        System.err.println(INITIAL_MESH_NOT_EMPTY);
      }
      Files.copy(Path.of(INITIAL_MESH), meshPath, StandardCopyOption.REPLACE_EXISTING);
    }

    void onMeshReload(@Observes MeshReloadUpdate event) throws Exception {
      Path meshPath = getMeshPath();

      switch (event.getEvent()) {
        case FULL_RELOAD_FINISHED -> {
          compareMeshContent(INITIAL_MESH);
          compareProjectDirectoryContent();

          Files.copy(Path.of(FAILING_MESH), meshPath, StandardCopyOption.REPLACE_EXISTING);
        }
        case INCREMENTAL_RELOAD_FAILED -> {
          Files.copy(Path.of(INCREMENTAL_RELOADED_MESH), meshPath,
              StandardCopyOption.REPLACE_EXISTING);
        }
        case INCREMENTAL_RELOAD_FINISHED -> {
          Files.deleteIfExists(meshPath);

          dashboardRunner.stopStreamxDashboard();
          meshStopper.scheduleStop();
        }
        default -> {

        }
      }
    }

    private @NotNull Path getMeshPath() {
      String meshPath = System.getProperty(MESH_PROPERTY_NAME);
      Path meshFile = Path.of(meshPath);
      return meshFile;
    }

    void onDashboardStarted(@Observes DashboardStarted event) {
      dashboardStarted.set(true);
    }

    private void compareProjectDirectoryContent() throws InterruptedException {
      var command = "ls -1 /data/project";

      var containerMeshContent = executeCommand(client, command);

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

    private void compareMeshContent(String mesh) throws InterruptedException, IOException {
      var command = "cat /data/mesh.yaml";

      var containerMeshContent = executeCommand(client, command);
      var meshPath = Paths.get(mesh);

      if (!Arrays.equals(Files.readAllBytes(meshPath), containerMeshContent)) {
        System.err.println(MESH_CONTENT_DIFFERENT);
      }
    }

    private byte[] executeCommand(DockerClient client, String command) throws InterruptedException {
      var execId = client
          .execCreateCmd("streamx-dashboard")
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

  public static class DevCommandProfile implements QuarkusTestProfile {

    @Override
    public Map<String, String> getConfigOverrides() {
      return Map.of(
          "streamx.dev.test.profile", "true",
          "streamx.container.startup-timeout-seconds", "15"
      );
    }
  }

}
