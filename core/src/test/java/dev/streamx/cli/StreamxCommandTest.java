package dev.streamx.cli;

import java.io.IOException;
import java.nio.file.Files;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StreamxCommandTest {

  public static final String TIMESTAMP_REGEX =
      "[0-9]{4}_[0-9]{2}_[0-9]{2}__[0-9]{2}_[0-9]{2}_[0-9]{2}_[0-9]{3}";
  private static final String STREAMX_LOG_REGEX =
      ".*/\\.streamx/logs/streamx-" + TIMESTAMP_REGEX + "\\.log";

  @Test
  void shouldOverrideProdFileLogName() throws IOException {
    // given
    String userHome = Files.createTempDirectory("").toFile().getAbsolutePath();
    System.clearProperty("%prod.quarkus.log.file.path");
    System.setProperty("user.home", userHome);

    // when
    StreamxCommand.main(new String[] {});

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
    StreamxCommand.main(new String[] {});

    // then
    String fileName = System.getProperty("%prod.quarkus.log.file.path");
    Assertions.assertEquals(".streamx.log", fileName);
  }
}
