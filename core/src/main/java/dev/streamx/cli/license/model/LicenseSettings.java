package dev.streamx.cli.license.model;

import java.util.List;
import java.util.Optional;

public record LicenseSettings(Optional<LastLicenseFetch> lastLicenseFetch,
                              List<LicenseApproval> licenseApprovals
) { }
