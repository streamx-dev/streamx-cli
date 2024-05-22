package dev.streamx.cli.exception;

import java.net.URI;

public class LicenceException extends RuntimeException {

  private LicenceException(String message, Exception exception) {
    super(message, exception);
  }

  private LicenceException(String message) {
    super(message);
  }

  public static LicenceException licenceFetchException(URI url) {
    return new LicenceException("""
        File '%s' couldn't be fetched.

        Make sure that:
         * there is internet connection,
         * there are no proxy/firewall issues to access %s,
         """.formatted(url, url));
  }

  public static LicenceException malformedLicenceException(URI url) {
    return new LicenceException("""
        File '%s' has malformed content.

        Make sure that:
         * your 'streamx' version is up to date.
         """.formatted(url, url));
  }

  public static LicenceException licenceAcceptanceRejectedException() {
    return new LicenceException("""
        License acceptance is required for using StreamX.""");
  }
}
