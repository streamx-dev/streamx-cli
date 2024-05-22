package dev.streamx.cli.licence;

import dev.streamx.cli.licence.model.LastLicenceFetch;
import dev.streamx.cli.licence.model.LicenceSettings;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;

@ApplicationScoped
class LicenceAcceptanceVerifier {

  @Inject
  LicenceContext licenceContext;

  boolean isAcceptanceRequired(LicenceSettings licenceSettings) {
    if (licenceContext.isAcceptLicence()) {
      return false;
    }

    String licenceUrlRequiredToBeAccepted = licenceSettings.lastLicenceFetch()
        .map(LastLicenceFetch::licenceUrl)
        .orElseThrow(() -> new IllegalStateException(
            "Acceptance is required only if it's preceded by successful fetch"));

    boolean requiredLicenceAccepted = licenceSettings.licenceApprovals().stream()
        .filter(Objects::nonNull)
        .anyMatch(licenceApproval ->
            StringUtils.equals(licenceApproval.url(), licenceUrlRequiredToBeAccepted));

    return !requiredLicenceAccepted;
  }
}
