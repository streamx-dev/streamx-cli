package dev.streamx.cli.licence.model;

import java.time.LocalDateTime;

public record LastLicenceFetch(LocalDateTime fetchDate,
                               String licenceName,
                               String licenceUrl
) { }
