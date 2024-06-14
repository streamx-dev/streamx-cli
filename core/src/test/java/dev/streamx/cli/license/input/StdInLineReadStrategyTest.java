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
      "\r\n",
      "y\n",
      "  y   \r\n",
      "\t y\r\n",
      "no way!\nI changed my mind...\ny\n",
      "no way!\r\nI changed my mind...\r\ny\r\n",
  })
  void shouldAccept(String source) {
    // given
    givenStdIn(source);

    // when
    boolean licenseAccepted = uut.isLicenseAccepted();

    // then
    Assertions.assertThat(licenseAccepted).isTrue();
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "n\n",
      "n\r\n",
      " \t  n   \r\n",
      "yes... but No!\nn\n",
  })
  void shouldReject(String source) {
    // given
    givenStdIn(source);

    // when
    boolean licenseAccepted = uut.isLicenseAccepted();

    // then
    Assertions.assertThat(licenseAccepted).isFalse();
  }

  private static void givenStdIn(String source) {
    System.setIn(new ByteArrayInputStream(source.getBytes(StandardCharsets.UTF_8)));
  }
}