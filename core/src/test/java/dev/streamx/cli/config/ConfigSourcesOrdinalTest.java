package dev.streamx.cli.config;

import static dev.streamx.cli.config.ConfigUtils.clearConfigCache;
import static dev.streamx.cli.config.ConfigUtils.clearConfigFile;
import static dev.streamx.cli.config.ConfigUtils.installFile;

import io.quarkus.test.junit.QuarkusTest;
import java.nio.file.Path;
import org.assertj.core.api.Assertions;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@QuarkusTest
class ConfigSourcesOrdinalTest {

  public static final String TESTED_PROPERTY = "propertySource";
  public static final String ARGUMENT_PROPERTY = "argumentProperty";
  public static final String STREAMX_HOME_PROPERTY = "streamxHome";

  private static String USER_HOME;

  @BeforeAll
  static void beforeAll() {
    USER_HOME = System.getProperty("user.home");
  }

  @AfterAll
  static void afterAll() {
    System.setProperty("user.home", USER_HOME);
  }

  @BeforeEach
  void setup() {
    clearConfigCache();
  }

  @AfterEach
  void shutdown() {
    ArgumentConfigSource.registerValue(TESTED_PROPERTY, null);
    System.clearProperty(TESTED_PROPERTY);
    clearConfigFile(".env");
    clearConfigFile("./config/application.properties");
    clearConfigFile("./target/.streamx/config/application.properties");
    clearConfigFile("./target/.streamx");
  }

  @ParameterizedTest
  @EnumSource(value = PropertySource.class, names = {
      "SYSTEM_PROPERTY",
      "DOT_ENV",
      "CONFIG_APPLICATION_PROPERTIES",
      "DOT_STREAMX",
      "CLASSPATH_APPLICATION_PROPERTIES"
  })
  void shouldBeOverriddenWithArgumentProperty(PropertySource propertySource) {
    // given
    populateWithArgument();
    propertySource.populate();

    // when
    String value = resolveTestProperty();

    // then
    Assertions.assertThat(value).isEqualTo(ARGUMENT_PROPERTY);
  }

  @ParameterizedTest
  @EnumSource(value = PropertySource.class, names = {
      "SYSTEM_PROPERTY",
      "DOT_ENV",
      "CONFIG_APPLICATION_PROPERTIES",
  })
  void shouldOverrideDotStreamX(PropertySource propertySource) {
    // given
    populateWithDotStreamx();
    propertySource.populate();

    // when
    String value = resolveTestProperty();

    // then
    Assertions.assertThat(value).isNotEqualTo(STREAMX_HOME_PROPERTY);
  }

  @ParameterizedTest
  @EnumSource(value = PropertySource.class, names = {
      "CLASSPATH_APPLICATION_PROPERTIES"
  })
  void shouldBeOverriddenWithDotStreamX(PropertySource propertySource) {
    // given
    populateWithDotStreamx();
    propertySource.populate();

    // when
    String value = resolveTestProperty();

    // then
    Assertions.assertThat(value).isEqualTo(STREAMX_HOME_PROPERTY);
  }

  private static void populateWithArgument() {
    ArgumentConfigSource.registerValue(TESTED_PROPERTY, ARGUMENT_PROPERTY);
  }

  private static void populateWithSystemProperty() {
    System.setProperty(TESTED_PROPERTY, "systemProperty");
  }

  private static void populateWithDotEnv() {
    installFile("./.env",
        "propertySource=dotEnv");
  }

  private static void populateWithConfigApplicationProperties() {
    installFile("./config/application.properties",
        "propertySource=configApplicationProperties");
  }

  private static void populateWithDotStreamx() {
    installFile("./target/.streamx/config/application.properties",
        "propertySource=" + STREAMX_HOME_PROPERTY);
    overriddenUserHome();
  }

  private static void overriddenUserHome() {
    String overriddenUserDir = Path.of("./target")
        .toAbsolutePath().normalize().toString();
    System.setProperty("user.home", overriddenUserDir);
  }

  private static String resolveTestProperty() {
    return ConfigProvider.getConfig().getValue(TESTED_PROPERTY, String.class);
  }

  enum PropertySource {
    SYSTEM_PROPERTY(ConfigSourcesOrdinalTest::populateWithSystemProperty),
    DOT_ENV(ConfigSourcesOrdinalTest::populateWithDotEnv),
    CONFIG_APPLICATION_PROPERTIES(
        ConfigSourcesOrdinalTest::populateWithConfigApplicationProperties),
    DOT_STREAMX(ConfigSourcesOrdinalTest::populateWithDotStreamx),
    CLASSPATH_APPLICATION_PROPERTIES(() -> {/* Already loaded */});

    private final Runnable runnable;

    PropertySource(Runnable runnable) {
      this.runnable = runnable;
    }

    public void populate() {
      runnable.run();
    }
  }
}
