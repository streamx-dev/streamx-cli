package dev.streamx.cli.config;

import static dev.streamx.cli.config.ConfigUtils.clearConfigCache;
import static dev.streamx.cli.config.ConfigUtils.installFile;

import io.quarkus.test.junit.QuarkusTest;
import java.nio.file.Path;
import org.assertj.core.api.Assertions;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

@QuarkusTest
class DotStreamxConfigSourceTest {

  private static String USER_HOME;

  @BeforeAll
  static void beforeAll() {
    USER_HOME = System.getProperty("user.home");
  }

  @AfterAll
  static void afterAll() {
    System.setProperty("user.home", USER_HOME);
  }

  @Test
  void shouldReadPropsFromDotStreamx() {
    // given
    clearConfigCache();
    installFile("./target/.streamx/config/application.properties",
        "userdir.property=UserdirValue");
    overriddenUserHome();

    // when
    String value = ConfigProvider.getConfig().getValue("userdir.property", String.class);

    // then
    Assertions.assertThat(value).isEqualTo("UserdirValue");
  }

  private static void overriddenUserHome() {
    String overriddenUserDir = Path.of("./target")
        .toAbsolutePath().normalize().toString();
    System.setProperty("user.home", overriddenUserDir);
  }
}
