package dev.streamx.cli.exception;

import java.net.URI;

public class LicenseException extends RuntimeException {

  private LicenseException(String message, Exception exception) {
    super(message, exception);
  }

  private LicenseException(String message) {
    super(message);
  }

  public static LicenseException licenseFetchException(URI url) {
    return new LicenseException("""
        File '%s' couldn't be fetched.

        Make sure that:
         * there is internet connection,
         * there are no proxy/firewall issues to access %s,
         """.formatted(url, url));
  }

  public static LicenseException malformedLicenseException(URI url) {
    return new LicenseException("""
        File '%s' has malformed content.

        Make sure that:
         * your 'streamx' version is up to date.
         """.formatted(url, url));
  }

  public static LicenseException licenseAcceptanceRejectedException() {
    return new LicenseException("""
        License acceptance is required for using StreamX.""");
  }
}
