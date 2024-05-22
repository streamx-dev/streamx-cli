package dev.streamx.cli.license.model;

import java.time.LocalDateTime;

public record LastLicenseFetch(LocalDateTime fetchDate,
                               String licenseName,
                               String licenseUrl
) { }
