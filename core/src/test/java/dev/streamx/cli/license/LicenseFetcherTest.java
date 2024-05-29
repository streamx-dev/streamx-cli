package dev.streamx.cli.license;

import dev.streamx.cli.license.LicenseFetcher.License;
import dev.streamx.cli.license.LicenseWiremockConfigs.StandardWiremockLicense;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

@QuarkusTest
@QuarkusTestResource(restrictToAnnotatedClass = true,
    value = StandardWiremockLicense.class)
class LicenseFetcherTest {

  @Inject
  LicenseFetcher cut;

  @Test
  void shouldFetchLicense() {
    // when
    License license = cut.fetchCurrentLicense();

    // then
    Assertions.assertThat(license).isNotNull();
    Assertions.assertThat(license.name()).isEqualTo("EULA");
    Assertions.assertThat(license.url()).isEqualTo("http://fake.streamx.dev/eula.html");
  }
}