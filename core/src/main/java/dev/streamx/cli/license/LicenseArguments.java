package dev.streamx.cli.license;

import jakarta.enterprise.inject.spi.CDI;
import picocli.CommandLine.Help.Visibility;
import picocli.CommandLine.Option;

public class LicenseArguments {

  @Option(names = "--accept-license",
      description = "Automatically accept actual StreamX license",
      showDefaultValue = Visibility.ALWAYS,
      defaultValue = "false")
  void propagateAcceptLicense(boolean acceptLicense) {
    LicenseContext context = CDI.current().select(LicenseContext.class).get();
    context.setAcceptLicense(acceptLicense);
  }
}

