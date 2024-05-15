package dev.streamx.cli.licence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import dev.streamx.cli.exception.LicenceException;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@QuarkusTest
@QuarkusTestResource(restrictToAnnotatedClass = true,
    value = LicenceWiremockConfigs.MalformedWiremockLicence.class)
class LicenceFetcherMalformedTest {

  @Inject
  LicenceFetcher cut;

  @Test
  void shouldHandleMalformedLicence() {
    // when
    Throwable throwable = catchThrowable(() -> cut.fetchCurrentLicence());

    // then
    assertThat(throwable).isInstanceOf(LicenceException.class);
    assertThat(throwable).hasMessageContainingAll("File '", "' has malformed content.");
  }
}