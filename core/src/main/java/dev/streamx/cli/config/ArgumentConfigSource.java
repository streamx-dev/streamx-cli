package dev.streamx.cli.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.SysPropConfigSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.config.spi.ConfigSource;

public class ArgumentConfigSource implements ConfigSource {

  public static final String CONFIG_SOURCE_NAME = ArgumentConfigSource.class.getSimpleName();
  /**
   * Value higher priority than {@link SysPropConfigSource System Properties}
   */
  public static final int ARGUMENT_PRIORITY = 500;

  private static final Map<String, String> configuration = new HashMap<>();
  private static boolean locked = false;

  @Override
  public int getOrdinal() {
    return ARGUMENT_PRIORITY;
  }

  @Override
  public Set<String> getPropertyNames() {
    return configuration.keySet();
  }

  @Override
  public String getValue(final String propertyName) {
    return configuration.get(propertyName);
  }

  @Override
  public String getName() {
    return CONFIG_SOURCE_NAME;
  }

  /**
    * WARNING!!!<br/>
    * Registered properties will be available for {@link ConfigMapping} or {@link ConfigProperty}
    * only if it's registered before Quarkus start.
    * (see {@link dev.streamx.cli.StreamxCommand#main(String[]) StreamxCommand})
    */
  public static void registerValue(String propertyName, String propertyValue) {
    if (!locked) {
      configuration.put(propertyName, propertyValue);
    }
  }

  public static void lock() {
    locked = true;
  }
}
