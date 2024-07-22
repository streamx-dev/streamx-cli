package dev.streamx.cli.test.tools.validators;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

import jakarta.enterprise.context.ApplicationScoped;
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

  public void validate(List<String> output, String expectedContent, long timeout) {
    try {
      await()
          .atMost(timeout, SECONDS)
          .pollInterval(100, MILLISECONDS)
          .alias("Finding expectedContent:" + expectedContent)
          .until(() ->
              output
                  .stream()
                  .anyMatch(line -> line.contains(expectedContent))
          );
    } catch (ConditionTimeoutException e) {
      logger.error(String.join("\n", output));
      throw e;
    }
  }

  public String validateContainsUrl(List<String> output, long timeout) {
    await()
        .atMost(timeout, SECONDS)
        .pollInterval(100, MILLISECONDS)
        .alias("Finding any url")
        .until(() ->
            output
                .stream()
                .anyMatch(line -> urlPattern.matcher(line).find())
        );

    return output.stream()
        .map(urlPattern::matcher)
        .filter(Matcher::find)
        .map(Matcher::group)
        .findFirst().orElseThrow();
  }
}
