package dev.streamx.cli.license;

import dev.streamx.cli.config.ArgumentConfigSource;
import org.apache.commons.lang3.BooleanUtils;
import picocli.CommandLine.Option;

public class LicenseArguments {

  @Option(names = "--accept-license",
      description = "Automatically accept the current StreamX license",
      defaultValue = "false")
  void propagateAcceptLicense(boolean acceptLicense) {
    ArgumentConfigSource.registerValue(LicenseConfig.STREAMX_ACCEPT_LICENSE,
        BooleanUtils.toStringTrueFalse(acceptLicense));
  }
}

