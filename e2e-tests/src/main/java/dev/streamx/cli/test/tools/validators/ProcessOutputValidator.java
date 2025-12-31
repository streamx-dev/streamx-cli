package dev.streamx.cli.test.tools.validators;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import dev.streamx.cli.test.tools.terminal.process.ShellProcess;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Duration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.awaitility.core.ConditionTimeoutException;
import org.jboss.logging.Logger;

@ApplicationScoped
public class ProcessOutputValidator {

  private final Logger logger = Logger.getLogger(ProcessOutputValidator.class);
  private static final Pattern urlPattern = Pattern.compile(
      "(?:^|[\\W])((ht|f)tp(s?):\\/\\/|www\\.)"
          + "(([\\w\\-]+\\.){1,}?([\\w\\-.~]+\\/?)*"
          + "[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\*$~@!:/{};']*)",
      Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);

  public void validate(ShellProcess process, List<String> output, String expectedContent,
      Duration timeout) {
    try {
      await()
          .atMost(timeout)
          .pollInterval(100, MILLISECONDS)
          .alias("Finding expectedContent: " + expectedContent)
          .untilAsserted(() ->
              assertThat(output)
                  .describedAs(() -> "Full output is:\n"
                                     + String.join("\n", process.getCurrentOutputLines())
                                     + "\n"
                                     + String.join("\n", process.getCurrentErrorLines()))
                  .anyMatch(line -> line.contains(expectedContent))
          );
    } catch (ConditionTimeoutException e) {
      logger.error(String.join("\n", output));
      throw e;
    }
  }

  public String validateContainsUrl(List<String> output, Duration timeout) {
    await()
        .atMost(timeout)
        .pollInterval(100, MILLISECONDS)
        .alias("Finding any url")
        .untilAsserted(() ->
            assertThat(output).anyMatch(line -> urlPattern.matcher(line).find())
        );

    return output.stream()
        .map(urlPattern::matcher)
        .filter(Matcher::find)
        .map(Matcher::group)
        .findFirst().orElseThrow();
  }
}
