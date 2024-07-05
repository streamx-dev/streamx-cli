package dev.streamx.cli.ingestion;

import dev.streamx.cli.config.ArgumentConfigSource;
import picocli.CommandLine.Help.Visibility;
import picocli.CommandLine.Option;

public class IngestionArguments {

  public static final String DEFAULT_INGESTION_URL = "http://localhost:8080";

  @Option(names = "--ingestion-url",
      description = "Address of 'ingestion-rest-service'",
      showDefaultValue = Visibility.ALWAYS,
      defaultValue = DEFAULT_INGESTION_URL)
  void propagateIngestionUrl(String ingestionUrl) {
    ArgumentConfigSource.registerValue(IngestionClientConfig.STREAMX_INGESTION_URL, ingestionUrl);
  }
}

