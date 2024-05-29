package dev.streamx.cli.license.source;

public class ConfigurableLicenseSource implements LicenseSource {

  private final String licenseUrl;

  public ConfigurableLicenseSource(String licenseUrl) {
    this.licenseUrl = licenseUrl;
  }

  @Override
  public String getUrl() {
    return licenseUrl;
  }
}
