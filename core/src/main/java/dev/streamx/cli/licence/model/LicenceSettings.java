package dev.streamx.cli.licence.model;

import java.time.LocalDateTime;
import java.util.List;

public record LicenceSettings(LocalDateTime lastFetchDate,
                              String lastFetchLicenceUrl,
                              List<LicenceApproval> licenceApprovals
) { }
