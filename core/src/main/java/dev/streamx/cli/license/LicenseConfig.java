package dev.streamx.cli.license;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "streamx")
public interface LicenseConfig {

  @WithDefault("false")
  boolean acceptLicense();
}
