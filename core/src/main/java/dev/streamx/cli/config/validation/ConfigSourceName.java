package dev.streamx.cli.config.validation;

import dev.streamx.cli.config.ArgumentConfigSource;
import dev.streamx.cli.config.DotStreamxConfigSource;

public enum ConfigSourceName {
  RUNTIME_OVERRIDE_CONFIG_SOURCE("overridden configuration",
      "Config Override Config Source"),
  SYSTEM_PROPERTIES_CONFIG_SOURCE("-D argument", "SysPropConfigSource"),
  ENV_CONFIG_SOURCE("environmental variable", "EnvConfigSource", null,
      300),
  ARGUMENT_CONFIG_SOURCE("option", ArgumentConfigSource.CONFIG_SOURCE_NAME),
  BUILD_TIME_RUNTIME_FIXED_CONFIG_SOURCE("BuildTime RunTime Fixed Config",
      "BuildTime RunTime Fixed"),
  DEFAULT_CONFIG_SOURCE("default config", "DefaultValuesConfigSource"),
  DOT_ENV_CONFIG_SOURCE(
      ".env file", "EnvConfigSource[source=file:/",
      "/.env]",
      295),
  CLASSPATH_PROPERTIES_CONFIG_SOURCE(
      "classpath application.properties file", "PropertiesConfigSource[source=",
      null,
      250),
  LOCAL_CONFIG_FILE_PROPERTIES_CONFIG_SOURCE(
      "./config/application.properties file", "PropertiesConfigSource[source=file:",
      "/config/application.properties]",
      260),
  DOT_STREAMX_CONFIG_SOURCE("${user.home}/.streamx/config/application.properties",
      DotStreamxConfigSource.CONFIG_SOURCE_NAME)
  ;

  private final String label;
  private final String namePrefix;
  private final String nameSuffix;
  private final Integer expectedOrdinal;

  ConfigSourceName(String label, String namePrefix, String nameSuffix, Integer expectedOrdinal) {
    this.label = label;
    this.namePrefix = namePrefix;
    this.nameSuffix = nameSuffix;
    this.expectedOrdinal = expectedOrdinal;
  }

  ConfigSourceName(String label, String namePrefix) {
    this.label = label;
    this.namePrefix = namePrefix;
    this.nameSuffix = null;
    this.expectedOrdinal = null;
  }

  public String getLabel() {
    return label;
  }

  public String getNamePrefix() {
    return namePrefix;
  }

  public String getNameSuffix() {
    return nameSuffix;
  }

  public Integer getExpectedOrdinal() {
    return expectedOrdinal;
  }
}
