package dev.streamx.cli.license.source;

public class ProdLicenseSource implements LicenseSource {

  public static final String PROD_LICENSE_SOURCE_URL =
      "https://raw.githubusercontent.com/streamx-dev/streamx-licenses/main/licenses.yml";

  @Override
  public String getUrl() {
    return PROD_LICENSE_SOURCE_URL;
  }
}
