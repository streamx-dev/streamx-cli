package dev.streamx.cli.licence.model;

import java.util.List;
import java.util.Optional;

public record LicenceSettings(Optional<LastLicenceFetch> lastLicenceFetch,
                              List<LicenceApproval> licenceApprovals
) { }
