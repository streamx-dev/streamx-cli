package dev.streamx.cli.config.validation;

import dev.streamx.cli.license.LicenseConfig;

public enum SecuredProperty {
  LICENSE_ACCEPT(LicenseConfig.STREAMX_ACCEPT_LICENSE,
      ConfigSourceName.DOT_ENV_CONFIG_SOURCE,
      ConfigSourceName.CLASSPATH_PROPERTIES_CONFIG_SOURCE,
      ConfigSourceName.LOCAL_CONFIG_FILE_PROPERTIES_CONFIG_SOURCE
  );

  private final String propertyName;
  private final ConfigSourceName[] forbiddenSources;

  SecuredProperty(String propertyName, ConfigSourceName... forbiddenSources) {
    this.propertyName = propertyName;
    this.forbiddenSources = forbiddenSources;
  }

  public String getPropertyName() {
    return propertyName;
  }

  public ConfigSourceName[] getForbiddenSources() {
    return forbiddenSources;
  }
}
