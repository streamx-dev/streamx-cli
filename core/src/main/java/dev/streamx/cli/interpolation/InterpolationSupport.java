package dev.streamx.cli.interpolation;

import static io.smallrye.common.expression.Expression.Flag.LENIENT_SYNTAX;
import static io.smallrye.common.expression.Expression.Flag.NO_SMART_BRACES;
import static io.smallrye.common.expression.Expression.Flag.NO_TRIM;

import io.smallrye.common.expression.Expression;
import jakarta.enterprise.context.Dependent;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.spi.ConfigProviderResolver;

@Dependent
public class InterpolationSupport {

  /**
   * Adapted from {@link io.smallrye.config.ExpressionConfigSourceInterceptor}
   */
  String expand(final String rawValue) {
    Objects.requireNonNull(rawValue, "Expression cannot be null");

    // Avoid extra StringBuilder allocations from Expression
    if (rawValue.indexOf('$') == -1) {
      return rawValue;
    }

    final Config config = ConfigProviderResolver.instance().getConfig();
    final Expression expression = Expression.compile(escapeDollarIfExists(rawValue), LENIENT_SYNTAX,
        NO_TRIM, NO_SMART_BRACES);
    return expression.evaluate((resolveContext, stringBuilder) -> {
      Optional<String> resolve = config.getOptionalValue(resolveContext.getKey(), String.class);
      if (resolve.isPresent()) {
        stringBuilder.append(resolve.get());
      } else if (resolveContext.hasDefault()) {
        resolveContext.expandDefault();
      } else {
        throw new NoSuchElementException(String.format("Could not expand value %s in expression %s",
            resolveContext.getKey(), rawValue));
      }
    });
  }

  private String escapeDollarIfExists(final String value) {
    int index = value.indexOf("\\$");
    if (index != -1) {
      int start = 0;
      StringBuilder builder = new StringBuilder();
      while (index != -1) {
        builder.append(value, start, index).append("$$");
        start = index + 2;
        index = value.indexOf("\\$", start);
      }
      builder.append(value.substring(start));
      return builder.toString();
    }
    return value;
  }
}
