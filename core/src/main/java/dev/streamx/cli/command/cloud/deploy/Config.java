package dev.streamx.cli.command.cloud.deploy;

import java.util.Map;

public record Config(String name, Map<String, String> data, ConfigType configType) {

  public enum ConfigType {
    DIR, FILE;

    public String getLabelValue() {
      return this.toString().toLowerCase();
    }
  }
}
