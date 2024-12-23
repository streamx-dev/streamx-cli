package dev.streamx.cli.command.cloud;

import static org.assertj.core.api.Assertions.assertThat;

import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainLauncher;
import io.quarkus.test.junit.main.QuarkusMainTest;
import org.junit.jupiter.api.Test;

@QuarkusMainTest
public class CloudCommandTest {

  @Test
  void shouldReturnMessageAboutSubcommands(QuarkusMainLauncher launcher) {
    LaunchResult result = launcher.launch("cloud");

    assertThat(result.getOutput()).contains(CloudCommand.MESSAGE);
  }
}
