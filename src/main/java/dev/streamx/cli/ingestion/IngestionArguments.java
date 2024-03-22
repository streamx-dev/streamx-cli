package dev.streamx.cli.ingestion;

import jakarta.enterprise.inject.spi.CDI;
import picocli.CommandLine.Help.Visibility;
import picocli.CommandLine.Option;

public class IngestionArguments {

    @Option(names = "--ingestion-url",
        description = "Address of 'rest-ingestion-service'",
        showDefaultValue = Visibility.ALWAYS,
        defaultValue = "http://localhost:8080")
    void propagateIngestionUrl(String ingestionUrl) {
      IngestionClientContext context = CDI.current().select(IngestionClientContext.class).get();
      context.setIngestionUrl(ingestionUrl);
    }
  }

