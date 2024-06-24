package dev.streamx.cli.license;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

@ConfigMapping
public interface LicenseConfig {

  String STREAMX_ACCEPT_LICENSE = "streamx.accept-license";

  @WithDefault("false")
  @WithName(STREAMX_ACCEPT_LICENSE)
  boolean acceptLicense();
}
