package dev.streamx.cli.licence;

import dev.streamx.cli.exception.LicenceException;
import dev.streamx.cli.licence.LicenceTestProfiles.RejectingLicenceTestProfile;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestProfile(RejectingLicenceTestProfile.class)
class LicenceRejectingTest {

  @Inject
  LicenceProcessorEntrypoint entrypoint;

  @Test
  void shouldRun() {
    // given
    // environment is clear

    // when
    Exception exception = Assertions.catchRuntimeException(() -> entrypoint.process());

    // then
    Assertions.assertThat(exception).isInstanceOf(LicenceException.class)
        .hasMessageContaining("License acceptance is required for using StreamX.");
  }
}