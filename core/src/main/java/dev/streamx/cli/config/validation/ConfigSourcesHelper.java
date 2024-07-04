package dev.streamx.cli.config.validation;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.jetbrains.annotations.NotNull;
import org.testcontainers.shaded.com.google.common.collect.HashMultimap;

@ApplicationScoped
class ConfigSourcesHelper {

  @NotNull
  HashMultimap<ConfigSourceName, ConfigSource> createConfigSourceMap() {
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

    return isStartMatched(sourceName, namePrefix)
        && isEndMatched(sourceName, nameSuffix)
        && isOrdinalMatched(source, ordinal);
  }

  private static boolean isStartMatched(String sourceName, String namePrefix) {
    return sourceName.startsWith(namePrefix);
  }

  private static boolean isEndMatched(String sourceName, String nameSuffix) {
    return nameSuffix == null || sourceName.endsWith(nameSuffix);
  }

  private static boolean isOrdinalMatched(ConfigSource source, Integer ordinal) {
    return ordinal == null || ordinal.equals(source.getOrdinal());
  }
}
