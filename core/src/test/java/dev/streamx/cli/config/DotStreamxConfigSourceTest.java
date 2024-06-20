package dev.streamx.cli.config;

import static java.nio.file.StandardOpenOption.CREATE;

import io.quarkus.runtime.configuration.QuarkusConfigFactory;
import io.quarkus.test.junit.QuarkusTest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.assertj.core.api.Assertions;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.Test;

@QuarkusTest
class DotStreamxConfigSourceTest {

  @Test
  void shouldReadPropsFromDotStreamx() throws IOException {
    // given
    installFile("./target/.streamx/config/application.properties",
        "userdir.property=UserdirValue");
    overriddenUserHome();

    // when
    String value = ConfigProvider.getConfig().getValue("userdir.property", String.class);

    // then
    Assertions.assertThat(value).isEqualTo("UserdirValue");
  }

  private static void overriddenUserHome() {
    QuarkusConfigFactory.setConfig(null);
    String overriddenUserDir = Path.of("./target/.streamx").normalize()
        .toAbsolutePath().toString();
    System.setProperty("user.home", overriddenUserDir);
  }

  public static void installFile(String path, String content) throws IOException {
    if (path.contains("/")) {
      Files.createDirectories(Path.of(path.substring(0, path.lastIndexOf("/"))));
    }

    Files.writeString(Path.of(path), content, CREATE);
  }
}