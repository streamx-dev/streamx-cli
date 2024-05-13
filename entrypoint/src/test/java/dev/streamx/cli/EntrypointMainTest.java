package dev.streamx.cli;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class EntrypointMainTest {

  private static final String STREAMX_LOG_REGEX =
      "\\.streamx/streamx-[0-9]{4}_[0-9]{2}_[0-9]{2}__[0-9]{2}_[0-9]{2}_[0-9]{2}_[0-9]{3}\\.log";

  @BeforeEach
  void setup() {
    StreamxCommand.clearLaunched();
  }

  @Test
  void shouldLaunchStreamxCommand() {
    // when
    EntrypointMain.main(new String[] {});

    // then
    Assertions.assertTrue(StreamxCommand.isLaunched());
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "1.8.0_211",
      "9.0.1",
      "11.0.4",
      "12",
      "12.0.1"
  })
  void shouldFailTooLowJavaVersions(String javaVersion) {
    // given
    System.clearProperty("java.version");
    System.setProperty("java.version", javaVersion);

    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(byteArrayOutputStream));

    // when
    EntrypointMain.main(new String[] {});

    // then
    Assertions.assertFalse(StreamxCommand.isLaunched());
    Assertions.assertEquals("Java 17 or higher is required!\n", byteArrayOutputStream.toString());
  }

  @Test
  void shouldOverrideProdFileLogName() {
    // given
    System.clearProperty("%prod.quarkus.log.file.path");

    // when
    EntrypointMain.main(new String[] {});

    // then
    String fileName = System.getProperty("%prod.quarkus.log.file.path");
    Assertions.assertTrue(fileName.matches(STREAMX_LOG_REGEX));
  }

  @Test
  void shouldUseProvidedProdFileLogName() {
    // given
    System.setProperty("%prod.quarkus.log.file.path", ".streamx.log");

    // when
    EntrypointMain.main(new String[] {});

    // then
    String fileName = System.getProperty("%prod.quarkus.log.file.path");
    Assertions.assertEquals(".streamx.log", fileName);
  }
}
