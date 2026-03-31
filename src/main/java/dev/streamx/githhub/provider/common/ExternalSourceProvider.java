package dev.streamx.githhub.provider.common;

import static dev.streamx.githhub.Constants.EXTERNAL_RESOURCE_URL;
import static dev.streamx.githhub.Constants.INGESTION_MESSAGE_KEY;
import static dev.streamx.githhub.Constants.PUBLISH_EVENT_TYPE;
import static dev.streamx.githhub.Constants.STREAMX_INGESTION_URL;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.streamx.exception.GitHubActionException;
import dev.streamx.githhub.provider.AbstractSourceProvider;
import dev.streamx.ingestion.IngestionConfig;
import dev.streamx.ingestion.payload.ExternalUrlPayload;
import io.cloudevents.CloudEvent;
import io.quarkiverse.githubaction.Context;
import io.quarkiverse.githubaction.Inputs;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import org.apache.http.impl.client.CloseableHttpClient;
import org.jboss.logging.Logger;
import org.kohsuke.github.GHEventPayload;

@ApplicationScoped
public class ExternalSourceProvider extends AbstractSourceProvider {

  public static final String NAME = "ExternalSourceProvider";

  private static final Logger log = Logger.getLogger(ExternalSourceProvider.class);

  private static final String[] PROVIDER_REQUIRED_INPUT_PARAMETERS = new String[]{
      STREAMX_INGESTION_URL, PUBLISH_EVENT_TYPE, EXTERNAL_RESOURCE_URL, INGESTION_MESSAGE_KEY};

  @Inject
  CloseableHttpClient httpClient;

  @Inject
  IngestionConfig ingestionConfig;

  ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public ObjectMapper getMapper() {
    return objectMapper;
  }

  @Override
  public List<CloudEvent> createPayload(Inputs inputs, Context context, GHEventPayload payload)
      throws GitHubActionException {
    assertProviderRequiredInputParameters(inputs, PROVIDER_REQUIRED_INPUT_PARAMETERS);

    String eventType = getInputString(inputs, PUBLISH_EVENT_TYPE);
    String externalUrl = getInputString(inputs, EXTERNAL_RESOURCE_URL);
    String messageKey = getInputString(inputs, INGESTION_MESSAGE_KEY);
    if (log.isDebugEnabled()) {
      log.debug("Creating ingestion payload for options:");
      log.debug("event type: " + eventType);
      log.debug("externalUrl: " + externalUrl);
      log.debug("key: " + messageKey);
    }
    ExternalUrlPayload externalUrlPayload = new ExternalUrlPayload(httpClient, eventType,
        externalUrl);
    externalUrlPayload.setKey(messageKey);
    externalUrlPayload.setSocketTimeout(ingestionConfig.connectionSocketTimeoutMillis());
    externalUrlPayload.setConnectionTimeout(ingestionConfig.connectionTimeoutMillis());

    CloudEvent event = externalUrlPayload.resolve();
    return List.of(event);
  }

}
