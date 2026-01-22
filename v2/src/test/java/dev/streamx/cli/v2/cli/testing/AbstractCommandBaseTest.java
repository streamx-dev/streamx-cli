package dev.streamx.cli.v2.cli.testing;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class AbstractCommandBaseTest {
  public final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
  public final ByteArrayOutputStream errStream = new ByteArrayOutputStream();

  @BeforeEach
  public void redirectStreams() {
    System.setOut(new PrintStream(outStream));
    System.setErr(new PrintStream(errStream));
  }

  @AfterEach
  public void restoreStreams() {
    outStream.reset();
    errStream.reset();

    System.setOut(System.out);
    System.setErr(System.err);
  }
}
