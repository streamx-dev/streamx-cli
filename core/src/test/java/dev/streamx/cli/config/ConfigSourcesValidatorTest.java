package dev.streamx.cli.config;

import static java.nio.file.StandardOpenOption.CREATE;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import dev.streamx.cli.exception.PropertiesException;
import io.quarkus.runtime.configuration.QuarkusConfigFactory;
import io.quarkus.test.junit.QuarkusTest;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@QuarkusTest
class ConfigSourcesValidatorTest {

  ConfigSourcesValidator uut = new ConfigSourcesValidator();

  @Test
  void shouldValidateStreamxAcceptLicenseProperty() {
    // when
    Throwable throwable = Assertions.catchThrowable(() -> uut.validate());

    // then
    Assertions.assertThat(throwable).isNull();
  }

  @ParameterizedTest
  @MethodSource(value = "securedPropertyParams")
  void shouldVerifyViolationOfSecuredProperty(String path, String content) throws IOException {
    try {
      // given
      QuarkusConfigFactory.setConfig(null);
      installFile(path, content);

      // when
      Throwable throwable = Assertions.catchThrowable(() -> uut.validate());
      // then
      Assertions.assertThat(throwable).isInstanceOf(PropertiesException.class);
    } finally {
      clearConfigFile(path);
    }
  }

  static Stream<Arguments> securedPropertyParams() {
    return Stream.of(
        arguments("./config/application.properties", "streamx.accept-license=true"),
        arguments("./.env", "STREAMX_ACCEPT-LICENSE=true")
    );
  }

  public static void installFile(String path, String content) throws IOException {
    if (path.contains("/")) {
      Files.createDirectories(Path.of(path.substring(0, path.lastIndexOf("/"))));
    }

    Files.writeString(Path.of(path), content, CREATE);
  }

  private static void clearConfigFile(String path) {
    File out = new File(path);
    if (out.isFile()) {
      out.delete();
    }
  }
}