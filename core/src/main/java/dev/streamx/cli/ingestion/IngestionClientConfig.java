package dev.streamx.cli.ingestion;

import static dev.streamx.cli.ingestion.IngestionArguments.DEFAULT_INGESTION_URL;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

@ConfigMapping
public interface IngestionClientConfig {

  String STREAMX_INGESTION_URL = "streamx.ingestion-url";

  @WithName(STREAMX_INGESTION_URL)
  @WithDefault(DEFAULT_INGESTION_URL)
  String ingestionUrl();
}
