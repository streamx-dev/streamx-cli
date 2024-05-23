package dev.streamx.cli.test.tools.validators;

import jakarta.enterprise.context.ApplicationScoped;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.jboss.logging.Logger;

@ApplicationScoped
public class ProcessValidator {

  private final Logger logger = Logger.getLogger(ProcessValidator.class);

  public void validateOutput(Process process, String expectedOutput, long timeoutInMs) {
    if (!waitForOutput(process.getInputStream(), expectedOutput, timeoutInMs)) {
      process.destroy();
      throw new AssertionError(
          "Process output did not match expected output: '" + expectedOutput + "' in " + timeoutInMs
              + " ms");
    }
  }

  private boolean waitForOutput(
      InputStream inputStream,
      String targetOutput,
      long timeoutInMs) {
    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
    long endTime = System.currentTimeMillis() + timeoutInMs;
    while (true) {
      if (System.currentTimeMillis() >= endTime) {
        return false;
      }
      try {
        String line = reader.readLine();
        if (line == null) {
          continue;
        }
        logger.error("OutputLine: " + line);
        if (line.contains(targetOutput)) {
          logger.info("terminal command target output line found: " + line);
          return true;
        }
      } catch (IOException e) {
        throw new RuntimeException("Cannot read output from inputstream", e);
      }
    }
  }
}
