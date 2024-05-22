package dev.streamx.cli.license;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class LicenseContext {

  private boolean acceptLicense;

  public boolean isAcceptLicenseFlagPresent() {
    return acceptLicense;
  }

  public void setAcceptLicense(boolean acceptLicense) {
    this.acceptLicense = acceptLicense;
  }
}
