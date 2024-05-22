package dev.streamx.cli.license;

import dev.streamx.cli.license.model.LastLicenseFetch;
import dev.streamx.cli.license.model.LicenseSettings;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;

@ApplicationScoped
class LicenseAcceptanceVerifier {

  @Inject
  LicenseContext licenseContext;

  boolean isAcceptanceRequired(LicenseSettings licenseSettings) {
    if (licenseContext.isAcceptLicenseFlagPresent()) {
      return false;
    }

    String licenseUrlRequiredToBeAccepted = licenseSettings.lastLicenseFetch()
        .map(LastLicenseFetch::licenseUrl)
        .orElseThrow(() -> new IllegalStateException(
            "Acceptance is required only if it's preceded by successful fetch"));

    boolean requiredLicenseAccepted = licenseSettings.licenseApprovals().stream()
        .filter(Objects::nonNull)
        .anyMatch(licenseApproval ->
            StringUtils.equals(licenseApproval.url(), licenseUrlRequiredToBeAccepted));

    return !requiredLicenseAccepted;
  }
}
