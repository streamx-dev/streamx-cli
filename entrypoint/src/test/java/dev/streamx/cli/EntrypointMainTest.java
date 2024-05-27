package dev.streamx.cli;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class EntrypointMainTest {

  public static final String TIMESTAMP_REGEX =
      "[0-9]{4}_[0-9]{2}_[0-9]{2}__[0-9]{2}_[0-9]{2}_[0-9]{2}_[0-9]{3}";
  private static final String STREAMX_LOG_REGEX =
      ".*/\\.streamx/logs/streamx-" + TIMESTAMP_REGEX + "\\.log";

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
    Assertions.assertTrue(
        byteArrayOutputStream.toString().contains("Java 17 or higher is required!")
    );
  }

  @Test
  void shouldOverrideProdFileLogName() throws IOException {
    // given
    String userHome = Files.createTempDirectory("").toFile().getAbsolutePath();
    System.clearProperty("%prod.quarkus.log.file.path");
    System.setProperty("user.home", userHome);

    // when
    EntrypointMain.main(new String[] {});

    // then
    String fileName = System.getProperty("%prod.quarkus.log.file.path");
    Assertions.assertTrue(fileName.matches(STREAMX_LOG_REGEX));
    Assertions.assertTrue(fileName.startsWith(userHome));
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
