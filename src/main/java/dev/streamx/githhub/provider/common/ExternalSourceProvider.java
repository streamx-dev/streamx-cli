package dev.streamx.githhub.provider.common;

import static dev.streamx.githhub.Constants.EXTERNAL_RESOURCE_URL;
import static dev.streamx.githhub.Constants.INGESTION_ACTION;
import static dev.streamx.githhub.Constants.INGESTION_CHANNEL;
import static dev.streamx.githhub.Constants.INGESTION_MESSAGE_KEY;
import static dev.streamx.githhub.Constants.INGESTION_TYPE;
import static dev.streamx.githhub.Constants.STREAMX_INGESTION_URL;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.streamx.exception.GitHubActionException;
import dev.streamx.githhub.provider.AbstractSourceProvider;
import dev.streamx.ingestion.IngestionConfig;
import dev.streamx.ingestion.payload.ExternalUrlPayload;
import dev.streamx.ingestion.schema.SchemaProvider;
import io.quarkiverse.githubaction.Context;
import io.quarkiverse.githubaction.Inputs;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Objects;
import org.apache.http.impl.client.CloseableHttpClient;
import org.jboss.logging.Logger;
import org.kohsuke.github.GHEventPayload;

@ApplicationScoped
public class ExternalSourceProvider extends AbstractSourceProvider {

  public static final String NAME = "ExternalSourceProvider";

  private static final Logger log = Logger.getLogger(ExternalSourceProvider.class);

  private static final String[] PROVIDER_REQUIRED_INPUT_PARAMETERS = new String[]{
      STREAMX_INGESTION_URL, INGESTION_CHANNEL, EXTERNAL_RESOURCE_URL, INGESTION_MESSAGE_KEY};

  @Inject
  CloseableHttpClient httpClient;

  @Inject
  IngestionConfig ingestionConfig;

  @Inject
  SchemaProvider schemaProvider;

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
  public List<JsonNode> createPayload(Inputs inputs, Context context, GHEventPayload payload)
      throws GitHubActionException {
    assertProviderRequiredInputParameters(inputs, PROVIDER_REQUIRED_INPUT_PARAMETERS);

    String schemaType = getIngestionSchemaType(schemaProvider, inputs);
    String action = getInputString(inputs, INGESTION_ACTION);
    String externalUrl = getInputString(inputs, EXTERNAL_RESOURCE_URL);
    String messageKey = getInputString(inputs, INGESTION_MESSAGE_KEY);
    if (log.isDebugEnabled()) {
      log.debug("Creating ingestion payload for options:");
      log.debug("action: " + action);
      log.debug("schema type: " + schemaType);
      log.debug("externalUrl: " + externalUrl);
      log.debug("key: " + messageKey);
    }
    ExternalUrlPayload externalUrlPayload = new ExternalUrlPayload(httpClient, action, externalUrl,
        schemaType);
    externalUrlPayload.setKey(messageKey);
    externalUrlPayload.setSocketTimeout(ingestionConfig.connectionSocketTimeoutMillis());
    externalUrlPayload.setConnectionTimeout(ingestionConfig.connectionTimeoutMillis());

    String ingestionType = getInputString(inputs, INGESTION_TYPE);
    if (Objects.nonNull(ingestionType)) {
      externalUrlPayload.setType(ingestionType);
    }

    JsonNode jsonNode = externalUrlPayload.resolve();
    return List.of(jsonNode);
  }

}
