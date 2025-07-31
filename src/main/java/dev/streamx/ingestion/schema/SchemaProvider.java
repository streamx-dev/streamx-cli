package dev.streamx.ingestion.schema;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.streamx.exception.GitHubActionException;
import dev.streamx.exception.UnknownChannelException;
import dev.streamx.ingestion.IngestionConfig;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.jboss.logging.Logger;

@ApplicationScoped
public class SchemaProvider {

  private static final Logger log = Logger.getLogger(SchemaProvider.class);

  private static final String UNKNOWN_CHANNEL_ERR_MSG_FMT =
      "Channel '%s' not found. Available channels are: [%s]";
  private static final String PAYLOAD_FIELD_NAME = "payload";

  @Inject
  IngestionConfig ingestionConfig;

  @Inject
  CloseableHttpClient httpClient;

  @Inject
  ObjectMapper objectMapper;

  public String getSchemaType(String ingestionUrl, String ingestionToken, String channel)
      throws GitHubActionException {
    JsonNode schemaJsonNode = getSchema(ingestionUrl, ingestionToken, channel);
    return getPayloadPropertyName(schemaJsonNode);
  }

  private String getPayloadPropertyName(JsonNode schemaJson) {
    Schema.Parser parser = new Schema.Parser();
    Schema channelSchema = parser.parse(schemaJson.toString());
    Field payload = channelSchema.getField(PAYLOAD_FIELD_NAME);
    Schema payloadSchema = payload.schema();
    if (Type.UNION == payloadSchema.getType()) {
      List<Schema> unionSchemas = payloadSchema.getTypes();
      for (Schema schema : unionSchemas) {
        if (Type.RECORD == schema.getType()) {
          return schema.getFullName();
        }
      }
    }
    return payloadSchema.getFullName();
  }

  private JsonNode getSchema(String ingestionUrl, String ingestionToken, String channel)
      throws GitHubActionException {
    Map<String, JsonNode> schemas = fetchSchemas(ingestionUrl, ingestionToken);
    if (schemas.containsKey(channel)) {
      return schemas.get(channel);
    }
    String supportedChannels = String.join(",", schemas.keySet());
    throw new UnknownChannelException(
        String.format(UNKNOWN_CHANNEL_ERR_MSG_FMT, channel, supportedChannels));
  }

  private Map<String, JsonNode> fetchSchemas(String ingestionUrl, String ingestionToken)
      throws GitHubActionException {
    URI ingestionChannelUri = buildUri(ingestionUrl);
    try {
      HttpGet httpRequest = new HttpGet(ingestionChannelUri);
      httpRequest.setConfig(RequestConfig.copy(RequestConfig.DEFAULT)
          .setConnectTimeout(ingestionConfig.connectionTimeoutMillis())
          .setSocketTimeout(ingestionConfig.connectionSocketTimeoutMillis())
          .build());
      addAuthorizationHeader(httpRequest, ingestionToken);
      HttpResponse response = httpClient.execute(httpRequest);
      verifyStatusCode(response);
      return readResponse(response);
    } catch (IOException exc) {
      String errMsg = "Error while fetching StreamX schemas: " + exc.getMessage();
      log.error(errMsg, exc);
      throw new GitHubActionException(errMsg);
    }
  }

  private Map<String, JsonNode> readResponse(HttpResponse response) throws IOException {
    HttpEntity entity = response.getEntity();
    String body = EntityUtils.toString(entity, StandardCharsets.UTF_8);
    return objectMapper.readValue(body, new TypeReference<>() {
    });
  }


  private void verifyStatusCode(HttpResponse execute) throws GitHubActionException {
    if (execute.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
      throw new GitHubActionException(
          // this message is copy-pasted from StreamxIngestionClient
          "Authentication failed. Make sure that the given token is valid.");
    }
  }

  private void addAuthorizationHeader(HttpGet httpRequest, String authToken) {
    if (StringUtils.isNotBlank(authToken)) {
      httpRequest.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + authToken);
    }
  }

  private URI buildUri(String ingestionUrl) throws GitHubActionException {
    String ingestionChannelsUrl = ingestionUrl + ingestionConfig.ingestionChannelsApi();
    try {
      return new URI(ingestionChannelsUrl);
    } catch (URISyntaxException e) {
      throw new GitHubActionException("Invalid ingestion channels url: " + ingestionChannelsUrl, e);
    }
  }
}
