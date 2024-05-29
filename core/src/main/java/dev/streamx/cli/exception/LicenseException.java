package dev.streamx.cli.exception;

import java.net.URI;

public class LicenseException extends RuntimeException {

  private LicenseException(String message, Exception exception) {
    super(message, exception);
  }

  private LicenseException(String message) {
    super(message);
  }

  public static LicenseException licenseFetchException() {
    return new LicenseException("""
        License could not be verified.

        Make sure that:
         * there is internet connection,
         * there are no proxy/firewall issues
         """);
  }

  public static LicenseException malformedLicenseException() {
    return new LicenseException("""
        License could not be verified.

        Make sure that:
         * your 'streamx' version is up to date.
         """);
  }

  public static LicenseException licenseAcceptanceRejectedException() {
    return new LicenseException("""
        License acceptance is required for using StreamX.""");
  }
}
