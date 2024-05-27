package dev.streamx.cli.license;

import dev.streamx.cli.exception.LicenseException;
import dev.streamx.cli.license.LicenseTestProfiles.RejectingLicenseTestProfile;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestProfile(RejectingLicenseTestProfile.class)
class LicenseRejectingTest {

  @Inject
  LicenseProcessorEntrypoint entrypoint;

  @Test
  void shouldRun() {
    // given
    respondWithRejectionIfUserIsAskedForAcceptance();

    // when
    Exception exception = Assertions.catchRuntimeException(() -> entrypoint.process());

    // then
    Assertions.assertThat(exception).isInstanceOf(LicenseException.class)
        .hasMessageContaining("License acceptance is required for using StreamX.");
  }

  private void respondWithRejectionIfUserIsAskedForAcceptance() {
    // this is assured by "streamx.cli.license.accepting-strategy.fixed.value=false"
    // setting from @TestProfile(RejectingLicenseTestProfile.class)
  }
}