package dev.streamx.cli.licence;

import jakarta.enterprise.inject.spi.CDI;
import picocli.CommandLine.Help.Visibility;
import picocli.CommandLine.Option;

public class LicenceArguments {

  @Option(names = "--accept-licence",
      description = "Automatically accept actual StreamX licence",
      showDefaultValue = Visibility.ALWAYS,
      defaultValue = "false")
  void propagateAcceptLicence(boolean acceptLicence) {
    LicenceContext context = CDI.current().select(LicenceContext.class).get();
    context.setAcceptLicence(acceptLicence);
  }
}

