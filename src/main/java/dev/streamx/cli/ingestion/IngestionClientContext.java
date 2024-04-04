package dev.streamx.cli.ingestion;

import static dev.streamx.cli.ingestion.IngestionArguments.DEFAULT_INGESTION_URL;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;

@ApplicationScoped
public class IngestionClientContext {

  private String ingestionUrl;
  private String oauth2Bearer;

  public String getIngestionUrl() {
    return Optional.ofNullable(ingestionUrl)
        .orElse(DEFAULT_INGESTION_URL);
  }

  public String getOauth2Bearer() {
    return oauth2Bearer;
  }

  public void setIngestionUrl(String ingestionUrl) {
    this.ingestionUrl = ingestionUrl;
  }
  public void setOauth2Bearer(String oauth2Bearer) {
    this.oauth2Bearer = oauth2Bearer;
  }
}
