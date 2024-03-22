package dev.streamx.cli.ingestion;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class IngestionClientContext {

  private String ingestionUrl;

  public String getIngestionUrl() {
    return ingestionUrl;
  }

  public void setIngestionUrl(String ingestionUrl) {
    this.ingestionUrl = ingestionUrl;
  }
}
