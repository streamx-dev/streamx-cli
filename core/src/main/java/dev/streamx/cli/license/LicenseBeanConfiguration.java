package dev.streamx.cli.license;

import static dev.streamx.cli.license.source.ProdLicenseSource.PROD_LICENSE_SOURCE_URL;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import dev.streamx.cli.license.input.AcceptingStrategy;
import dev.streamx.cli.license.input.StdInLineReadStrategy;
import dev.streamx.cli.license.proceeding.LicenseProceedingStrategy;
import dev.streamx.cli.license.proceeding.ProceedingLicenseDisabled;
import dev.streamx.cli.license.proceeding.ProceedingLicenseEnabled;
import dev.streamx.cli.license.source.ConfigurableLicenseSource;
import dev.streamx.cli.license.source.LicenseSource;
import dev.streamx.cli.license.source.ProdLicenseSource;
import io.quarkus.arc.DefaultBean;
import io.quarkus.arc.profile.IfBuildProfile;
import io.quarkus.arc.properties.IfBuildProperty;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Dependent
class LicenseBeanConfiguration {

  @Produces
  @Singleton
  @LicenseProcessing
  ObjectMapper licensesProcessingObjectMapper() {
    return new ObjectMapper(new YAMLFactory());
  }

  @Produces
  @DefaultBean
  AcceptingStrategy stdInLineReadValue() {
    return new StdInLineReadStrategy();
  }

  @Produces
  @DefaultBean
  LicenseProceedingStrategy proceedingLicenseEnabled() {
    return new ProceedingLicenseEnabled();
  }

  @Produces
  @IfBuildProperty(name = "streamx.cli.license.proceeding.enabled", stringValue = "false")
  LicenseProceedingStrategy proceedingLicenseDisabled() {
    return new ProceedingLicenseDisabled();
  }

  @Produces
  @IfBuildProfile("prod")
  LicenseSource prodLicenseSource() {
    return new ProdLicenseSource();
  }

  @Produces
  @DefaultBean
  LicenseSource configurableLicenseSource(
      @ConfigProperty(name = "streamx.cli.license.current-license-url",
          defaultValue = PROD_LICENSE_SOURCE_URL)
      String url
  ) {
    return new ConfigurableLicenseSource(url);
  }
}
