package dev.streamx.cli.ingestion;

import jakarta.enterprise.inject.spi.CDI;
import picocli.CommandLine.Help.Visibility;
import picocli.CommandLine.Option;

public class IngestionArguments {

  public static final String DEFAULT_INGESTION_URL = "http://localhost:8080";

  @Option(names = "--ingestion-url",
        description = "Address of 'rest-ingestion-service'",
        showDefaultValue = Visibility.ALWAYS,
        defaultValue = DEFAULT_INGESTION_URL)
  void propagateIngestionUrl(String ingestionUrl) {
    IngestionClientContext context = CDI.current().select(IngestionClientContext.class).get();
    context.setIngestionUrl(ingestionUrl);
  }

  @Option(names = "--oauth2-bearer",
        description = "Authorization for 'rest-ingestion-service'")
  void propagateOauth2Bearer(String oauth2Bearer) {
    IngestionClientContext context = CDI.current().select(IngestionClientContext.class).get();
    context.setOauth2Bearer(oauth2Bearer);
  }
}
