package dev.streamx.cli.licence;

import dev.streamx.cli.licence.LicenceFetcher.Licence;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

@QuarkusTest
@QuarkusTestResource(restrictToAnnotatedClass = true,
    value = LicenceWiremockConfigs.StandardWiremockLicence.class)
class LicenceFetcherTest {

  @Inject
  LicenceFetcher cut;

  @Test
  void shouldFetchLicence() {
    // when
    Licence licence = cut.fetchCurrentLicence();

    // then
    Assertions.assertThat(licence).isNotNull();
    Assertions.assertThat(licence.name()).isEqualTo("EULA");
    Assertions.assertThat(licence.url()).isEqualTo("http://fake.streamx.dev/eula.html");
  }
}