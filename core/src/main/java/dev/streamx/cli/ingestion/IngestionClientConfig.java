package dev.streamx.cli.ingestion;

import static dev.streamx.cli.ingestion.IngestionArguments.DEFAULT_INGESTION_URL;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "streamx")
public interface IngestionClientConfig {

  @WithDefault(DEFAULT_INGESTION_URL)
  String ingestionUrl();
}
