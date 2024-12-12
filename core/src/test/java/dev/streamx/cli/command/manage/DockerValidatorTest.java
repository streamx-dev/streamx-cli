package dev.streamx.cli.command.manage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchException;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.api.command.RemoveContainerCmd;
import com.github.dockerjava.api.exception.ConflictException;
import dev.streamx.runner.validation.excpetion.DockerContainerNonUniqueException;
import dev.streamx.runner.validation.excpetion.DockerEnvironmentException;
import io.quarkus.arc.impl.Reflections;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.testcontainers.DockerClientFactory;


class DockerValidatorTest {

  public static final String HELLO_WORLD_IMAGE = "hello-world:latest";
  public static final String HELLO_WORLD_CONTAINER_NAME = "dockerValidatorTestContainer";

  private final DockerValidator uut = new DockerValidator();

  @BeforeAll
  static void initialize() throws InterruptedException {
    DockerClient client = DockerClientFactory.instance().client();
    prepareContainer(client, HELLO_WORLD_CONTAINER_NAME);
  }

  @AfterAll
  static void destroy() {
    DockerClient client = DockerClientFactory.instance().client();
    dropContainer(client, HELLO_WORLD_CONTAINER_NAME).exec();
  }

  @Test
  void shouldDetectDuplicatedContainers() {
    // given
    var usedContainers = Set.of(HELLO_WORLD_CONTAINER_NAME);

    // when
    var exception = catchException(() -> uut.validateDockerEnvironment(usedContainers));

    // then
    assertThat(exception)
        .isInstanceOf(DockerContainerNonUniqueException.class)
        .hasMessage("Containers with names '" + HELLO_WORLD_CONTAINER_NAME + "' already exists.");
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "RunnerValidatorTest",
      "streamx",
      "Container",
  })
  void shouldNotDetectDuplicatedContainers(String testedContainerName) {
    // given
    var usedContainers = Set.of(testedContainerName);

    // when
    var exception = catchException(() -> uut.validateDockerEnvironment(usedContainers));

    // then
    assertThat(exception).isNull();
  }

  private static RemoveContainerCmd dropContainer(DockerClient client,
      String containerName) {
    return client.removeContainerCmd(containerName);
  }

  private static void prepareContainer(DockerClient client, String containerName)
      throws InterruptedException {
    client.pullImageCmd(HELLO_WORLD_IMAGE)
        .exec(new PullImageResultCallback())
        .awaitCompletion(30, TimeUnit.SECONDS);

    try {
      client.createContainerCmd(HELLO_WORLD_IMAGE)
          .withName(containerName)
          .exec();
    } catch (ConflictException e) {
      // container already exists
    }
  }

  @Test
  void shouldValidateNoDockerEnvironment() {
    DockerClientFactory dockerClientFactory = DockerClientFactory.instance();

    try {
      // given
      DockerEnvironmentException dockerEnvironmentException =
          new DockerEnvironmentException(new RuntimeException());
      Reflections.writeField(DockerClientFactory.class, "cachedClientFailure",
          dockerClientFactory, dockerEnvironmentException);

      // when
      Exception exception = catchException(() -> uut.validateDockerEnvironment(Mockito.mock()));

      // then
      assertThat(exception)
          .isInstanceOf(DockerEnvironmentException.class);
    } finally {
      Reflections.writeField(DockerClientFactory.class, "cachedClientFailure",
          dockerClientFactory, null);
    }
  }
}
