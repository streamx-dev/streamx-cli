package dev.streamx.cli.licence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import dev.streamx.cli.licence.input.AcceptingStrategy;
import dev.streamx.cli.licence.input.FixedValueStrategy;
import dev.streamx.cli.licence.input.StdInLineReadStrategy;
import io.quarkus.arc.DefaultBean;
import io.quarkus.arc.profile.IfBuildProfile;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Dependent
class LicenceConfig {

  @ApplicationScoped
  @LicenceProcessing
  ObjectMapper licencesProcessingObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());

    return objectMapper;
  }

  @Produces
  @IfBuildProfile("test")
  AcceptingStrategy fixedValue(
      @ConfigProperty(
          name = "streamx.cli.licence.accepting-strategy.fixed.value",
          defaultValue = "true"
      ) boolean value) {
    return new FixedValueStrategy(value);
  }

  @Produces
  @DefaultBean
  AcceptingStrategy stdInLineReadValue() {
    return new StdInLineReadStrategy();
  }
}
