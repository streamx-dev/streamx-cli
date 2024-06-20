package dev.streamx.cli.config;

import static dev.streamx.cli.config.ArgumentConfigSource.CONFIG_SOURCE_NAME;

import dev.streamx.cli.exception.PropertiesException;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.jetbrains.annotations.NotNull;
import org.testcontainers.shaded.com.google.common.collect.HashMultimap;

@ApplicationScoped
public class ConfigSourcesValidator {

  public void validate() {
    var configSources = createConfigSourceMap();
    var propertiesFoundInForbiddenConfigSources = findForbiddenConfigEntries(configSources);

    if (!propertiesFoundInForbiddenConfigSources.isEmpty()) {
      throw PropertiesException.propertiesFoundInForbiddenSources(
          propertiesFoundInForbiddenConfigSources);
    }
  }


  @NotNull
  private static HashMultimap<ConfigSourceName, ConfigSource> createConfigSourceMap() {
    return ConfigSourcesHelper.createConfigSourceMap();
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

  private static class ConfigSourcesHelper {
    @NotNull
    private static HashMultimap<ConfigSourceName, ConfigSource> createConfigSourceMap() {
      var configSources = HashMultimap.<ConfigSourceName, ConfigSource>create();
      Iterable<ConfigSource> sources = ConfigProvider.getConfig().getConfigSources();
      for (var source : sources) {
        String sourceName = source.getName();
        for (var configSourceName : ConfigSourceName.values()) {
          boolean matched = matchSources(source, configSourceName, sourceName);
          if (matched) {
            configSources.put(configSourceName, source);
            break;
          }
        }
      }
      return configSources;
    }

    private static boolean matchSources(ConfigSource source, ConfigSourceName configSourceName,
        String sourceName) {
      var namePrefix = configSourceName.getNamePrefix();
      var nameSuffix = configSourceName.getNameSuffix();
      var ordinal = configSourceName.getExpectedOrdinal();

      return startMatched(sourceName, namePrefix)
          && endMatched(sourceName, nameSuffix)
          && ordinalMatched(source, ordinal);
    }

    private static boolean startMatched(String sourceName, String namePrefix) {
      return sourceName.startsWith(namePrefix);
    }

    private static boolean ordinalMatched(ConfigSource source, Integer ordinal) {
      return ordinal == null || ordinal.equals(source.getOrdinal());
    }

    private static boolean endMatched(String sourceName, String nameSuffix) {
      return nameSuffix == null || sourceName.endsWith(nameSuffix);
    }
  }

  public enum SecuredProperty {
    LICENSE_ACCEPT("streamx.accept-license",
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

  public enum ConfigSourceName {
    RUNTIME_OVERRIDE_CONFIG_SOURCE("overridden configuration", "Config Override Config Source"),
    SYSTEM_PROPERTIES_CONFIG_SOURCE("-D argument", "SysPropConfigSource"),
    ENV_CONFIG_SOURCE("environmental variable", "EnvConfigSource", null, 300),
    ARGUMENT_CONFIG_SOURCE("option", CONFIG_SOURCE_NAME),
    BUILD_TIME_RUNTIME_FIXED_CONFIG_SOURCE("", "BuildTime RunTime Fixed"),
    DEFAULT_CONFIG_SOURCE("de", "DefaultValuesConfigSource"),
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
    // TODO add HOME_DIR_DOT_STREAMX_CONFIG_PROPS
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
}
