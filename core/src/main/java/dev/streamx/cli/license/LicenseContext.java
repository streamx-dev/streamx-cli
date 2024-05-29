package dev.streamx.cli.license;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class LicenseContext {

  private boolean acceptLicenseFlag;

  public boolean isAcceptLicenseFlagSet() {
    return acceptLicenseFlag;
  }

  public void setAcceptLicenseFlag(boolean acceptLicense) {
    this.acceptLicenseFlag = acceptLicense;
  }
}
