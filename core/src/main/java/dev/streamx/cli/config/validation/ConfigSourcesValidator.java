package dev.streamx.cli.config.validation;

import dev.streamx.cli.exception.PropertiesException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.jetbrains.annotations.NotNull;
import org.testcontainers.shaded.com.google.common.collect.HashMultimap;

@ApplicationScoped
public class ConfigSourcesValidator {

  @Inject
  ConfigSourcesHelper helper;

  public void validate() {
    var configSources = createConfigSourceMap();
    var propertiesFoundInForbiddenConfigSources = findForbiddenConfigEntries(configSources);

    if (!propertiesFoundInForbiddenConfigSources.isEmpty()) {
      throw PropertiesException.propertiesFoundInForbiddenSources(
          propertiesFoundInForbiddenConfigSources);
    }
  }

  @NotNull
  private HashMultimap<ConfigSourceName, ConfigSource> createConfigSourceMap() {
    return helper.createConfigSourceMap();
  }

  @NotNull
  private static List<SecuredProperty> findForbiddenConfigEntries(
      HashMultimap<ConfigSourceName, ConfigSource> configSources) {
    var propertiesFoundInForbiddenConfigSources = new ArrayList<SecuredProperty>();

    for (var securedProperty : SecuredProperty.values()) {
      for (var forbiddenSource : securedProperty.getForbiddenSources()) {
        for (var configSource : configSources.get(forbiddenSource)) {
          fillList(securedProperty, configSource, propertiesFoundInForbiddenConfigSources);
        }
      }
    }

    return propertiesFoundInForbiddenConfigSources;
  }

  private static void fillList(SecuredProperty securedProperty, ConfigSource configSource,
      List<SecuredProperty> propertiesFoundInForbiddenConfigSources) {
    if (configSource != null) {
      String propertyName = securedProperty.getPropertyName();
      if (configSource.getValue(propertyName) != null) {
        propertiesFoundInForbiddenConfigSources.add(securedProperty);
      }
    }
  }
}
