package dev.streamx.cli.license;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import dev.streamx.cli.exception.LicenseException;
import dev.streamx.cli.license.LicenseWiremockConfigs.TimeoutWiremockLicense;
import io.quarkus.test.common.WithTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@QuarkusTest
@WithTestResource(TimeoutWiremockLicense.class)
class LicenseFetcherTimeoutTest {

  @Inject
  LicenseFetcher cut;

  @Test
  void shouldHandleTimeoutWhileFetchLicense() {
    // when
    Throwable throwable = catchThrowable(() -> cut.fetchCurrentLicense());

    // then
    assertThat(throwable).isInstanceOf(LicenseException.class);
    assertThat(throwable).hasMessageContainingAll("License could not be verified.");
  }
}