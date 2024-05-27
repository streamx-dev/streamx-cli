package dev.streamx.cli.license;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import dev.streamx.cli.exception.LicenseException;
import dev.streamx.cli.license.LicenseWiremockConfigs.MalformedWiremockLicense;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@QuarkusTest
@QuarkusTestResource(restrictToAnnotatedClass = true,
    value = MalformedWiremockLicense.class)
class LicenseFetcherMalformedTest {

  @Inject
  LicenseFetcher cut;

  @Test
  void shouldHandleMalformedLicense() {
    // when
    Throwable throwable = catchThrowable(() -> cut.fetchCurrentLicense());

    // then
    assertThat(throwable).isInstanceOf(LicenseException.class);
    assertThat(throwable).hasMessageContainingAll("License could not be verified.");
  }
}