package dev.streamx.cli.exception;

import dev.streamx.cli.config.ConfigSourcesValidator.ConfigSourceName;
import dev.streamx.cli.config.ConfigSourcesValidator.SecuredProperty;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PropertiesException extends RuntimeException {

  private PropertiesException(String message) {
    super(message);
  }

  public static PropertiesException propertiesFoundInForbiddenSources(
      List<SecuredProperty> securedProperties) {
    String propertiesMessage = securedProperties.stream()
        .map(prop -> {
          var sources = Arrays.stream(prop.getForbiddenSources())
              .map(ConfigSourceName::getLabel)
              .map(label -> "\t * " + label)
              .collect(Collectors.joining(",\n"));

          return (" * property '%s' must not be defined in:\n%s")
              .formatted(prop.getPropertyName(), sources);
        })
        .collect(Collectors.joining("\n"));

    return new PropertiesException("""
        Illegal configuration found.
        %s
        
        Remove properties from forbidden sources."""
        .formatted(propertiesMessage));
  }
}
