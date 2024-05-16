package dev.streamx.cli.licence.model;

import java.time.LocalDateTime;

public record LicenceApproval(
    LocalDateTime approvalDate,
    String name,
    String url
) { }
