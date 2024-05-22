package dev.streamx.cli.license.model;

import java.time.LocalDateTime;

public record LicenseApproval(
    LocalDateTime approvalDate,
    String name,
    String url
) { }
