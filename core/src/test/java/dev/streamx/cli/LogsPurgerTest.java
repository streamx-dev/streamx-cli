package dev.streamx.cli;

import io.quarkus.test.component.QuarkusComponentTest;
import io.quarkus.test.component.TestConfigProperty;
import jakarta.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@QuarkusComponentTest
@TestConfigProperty(key = "logs.purging.enabled", value = "true")
class LogsPurgerTest {

  @TempDir
  Path temp;

  private static String USER_HOME;

  @Inject
  LogsPurger cut;

  @BeforeAll
  static void beforeAll() {
    USER_HOME = System.getProperty("user.home");
  }

  @AfterAll
  static void afterAll() {
    System.setProperty("user.home", USER_HOME);
  }

  @Test
  void test() throws IOException {
    // given
    System.setProperty("user.home", temp.toAbsolutePath().normalize().toString());
    Path logs = temp.resolve(".streamx").resolve("logs");
    Files.createDirectories(logs);
    Path log = logs.resolve("some.log");
    Files.createFile(log);
    log.toFile().setLastModified(0L);

    // when
    cut.purge();

    // then
    Assertions.assertThat(Files.exists(log)).isFalse();
  }
}
