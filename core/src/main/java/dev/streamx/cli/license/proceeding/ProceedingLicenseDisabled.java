package dev.streamx.cli.license.proceeding;

public class ProceedingLicenseDisabled implements LicenseProceedingStrategy {

  @Override
  public boolean isEnabled() {
    return false;
  }
}
