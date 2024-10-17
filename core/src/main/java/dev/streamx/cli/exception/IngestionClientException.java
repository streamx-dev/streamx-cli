package dev.streamx.cli.exception;

import static dev.streamx.cli.ingestion.IngestionClientConfig.STREAMX_INGESTION_INSECURE;

import dev.streamx.cli.util.ExceptionUtils;

public class IngestionClientException extends RuntimeException {

  private IngestionClientException(String message, Exception exception) {
    super(message, exception);
  }

  private IngestionClientException(String message) {
    super(message);
  }

  public static IngestionClientException sslException(String url) {
    return new IngestionClientException(ExceptionUtils.appendLogSuggestion("""
        Certificate validation failed for URL '%s'.

        Make sure that:
         * the ingestion endpoint is secured with a valid certificate.
         
        If you want to skip certificate validation, set the '%s' property to 'true'."""
        .formatted(url, STREAMX_INGESTION_INSECURE)));
  }
}
