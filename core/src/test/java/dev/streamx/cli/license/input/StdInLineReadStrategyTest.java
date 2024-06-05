package dev.streamx.cli.license.input;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class StdInLineReadStrategyTest {

  private StdInLineReadStrategy uut = new StdInLineReadStrategy();

  @ParameterizedTest
  @ValueSource(strings = {
      "\n",
      "y\n",
      "no way!\nI changed my mind...\ny\n",
  })
  void shouldAccept(String source) {
    // given
    System.setIn(new ByteArrayInputStream(source.getBytes(StandardCharsets.UTF_8)));

    // when
    boolean licenseAccepted = uut.isLicenseAccepted();

    // then
    Assertions.assertThat(licenseAccepted).isTrue();
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "n\n",
      "yes... but No!\nn\n",
  })
  void shouldReject(String source) {
    // given
    System.setIn(new ByteArrayInputStream(source.getBytes(StandardCharsets.UTF_8)));

    // when
    boolean licenseAccepted = uut.isLicenseAccepted();

    // then
    Assertions.assertThat(licenseAccepted).isFalse();
  }
}