package dev.streamx.cli.license;

import dev.streamx.cli.config.ArgumentConfigSource;
import org.apache.commons.lang3.BooleanUtils;
import picocli.CommandLine.Help.Visibility;
import picocli.CommandLine.Option;

public class LicenseArguments {

  @Option(names = "--accept-license",
      description = "Automatically accept actual StreamX license",
      showDefaultValue = Visibility.ALWAYS,
      defaultValue = "false")
  void propagateAcceptLicense(boolean acceptLicense) {
    ArgumentConfigSource.registerValue("streamx.accept-license",
        BooleanUtils.toStringTrueFalse(acceptLicense));
  }
}

