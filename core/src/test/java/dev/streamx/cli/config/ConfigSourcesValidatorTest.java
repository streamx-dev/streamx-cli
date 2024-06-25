package dev.streamx.cli.config;

import static dev.streamx.cli.config.ConfigUtils.clearConfigCache;
import static dev.streamx.cli.config.ConfigUtils.clearConfigFile;
import static dev.streamx.cli.config.ConfigUtils.installFile;
import static dev.streamx.cli.ingestion.IngestionClientConfig.STREAMX_AUTH_TOKEN;
import static dev.streamx.cli.license.LicenseConfig.STREAMX_ACCEPT_LICENSE;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import dev.streamx.cli.config.validation.ConfigSourcesValidator;
import dev.streamx.cli.exception.PropertiesException;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.stream.Stream;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@QuarkusTest
class ConfigSourcesValidatorTest {

  @Inject
  ConfigSourcesValidator uut;

  @BeforeEach
  void setup() {
    clearConfigCache();
  }

  @Test
  void shouldValidateStreamxAcceptLicenseProperty() {
    // when
    Throwable throwable = Assertions.catchThrowable(() -> uut.validate());

    // then
    Assertions.assertThat(throwable).isNull();
  }

  @ParameterizedTest
  @MethodSource(value = "securedPropertyParams")
  void shouldVerifyViolationOfSecuredProperty(String path, String content) {
    try {
      // given
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
        arguments("./config/application.properties", STREAMX_ACCEPT_LICENSE + "=true"),
        arguments("./config/application.properties", STREAMX_AUTH_TOKEN + "=auth_token")
    );
  }
}