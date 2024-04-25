package dev.streamx.cli.ingestion;

import static dev.streamx.cli.ingestion.IngestionArguments.DEFAULT_INGESTION_URL;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;

@ApplicationScoped
public class IngestionClientContext {

  private String ingestionUrl;

  public String getIngestionUrl() {
    return Optional.ofNullable(ingestionUrl)
        .orElse(DEFAULT_INGESTION_URL);
  }

  public void setIngestionUrl(String ingestionUrl) {
    this.ingestionUrl = ingestionUrl;
  }
}
