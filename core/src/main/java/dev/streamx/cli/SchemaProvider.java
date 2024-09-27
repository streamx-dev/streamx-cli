package dev.streamx.cli;

import static dev.streamx.cli.util.ExceptionUtils.sneakyThrow;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.streamx.cli.exception.IngestionClientException;
import dev.streamx.cli.exception.UnableToConnectIngestionServiceException;
import dev.streamx.cli.exception.UnknownChannelException;
import dev.streamx.cli.ingestion.IngestionClientConfig;
import dev.streamx.clients.ingestion.exceptions.StreamxClientException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import javax.net.ssl.SSLHandshakeException;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

@ApplicationScoped
public class SchemaProvider {

  private static final ObjectMapper objectMapper = new ObjectMapper();

  @Inject
  CloseableHttpClient httpClient;

  @Inject
  IngestionClientConfig ingestionClientConfig;

  public JsonNode getSchema(String channel) throws UnknownChannelException {
    Map<String, JsonNode> schemas = fetchSchemas(ingestionClientConfig.url());
    if (schemas.containsKey(channel)) {
      return schemas.get(channel);
    }
    throw new UnknownChannelException(channel, schemas.keySet().toString());
  }

  private Map<String, JsonNode> fetchSchemas(String ingestionUrl) {
    try {
      URI channelsEndpointUri = buildUri(ingestionUrl);
      HttpGet httpRequest = new HttpGet(channelsEndpointUri);

      ingestionClientConfig.authToken().or(ingestionClientConfig::rootAuthToken)
          .ifPresent(authToken -> addAuthorizationHeader(httpRequest, authToken));

      HttpResponse execute = httpClient.execute(httpRequest);
      verifyStatusCode(execute);

      HttpEntity entity = execute.getEntity();

      String body = EntityUtils.toString(entity, "UTF-8");

      return objectMapper.readValue(body, new TypeReference<>() {
      });
    } catch (SSLHandshakeException e) {
      throw IngestionClientException.sslException(ingestionClientConfig.url());
    } catch (IOException e) {
      throw new UnableToConnectIngestionServiceException(ingestionClientConfig.url(), e);
    } catch (StreamxClientException e) {
      throw sneakyThrow(e);
    }
  }

  private static void verifyStatusCode(HttpResponse execute) throws StreamxClientException {
    if (execute.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
      throw new StreamxClientException(
          // this message is copy-pasted from StreamxIngestionClient
          "Authentication failed. Make sure that the given token is valid.");
    }
  }

  private URI buildUri(String publicationsEndpointUri) {
    String uriString = String.format("%s/ingestion/v1/channels", publicationsEndpointUri);
    try {
      return new URI(uriString);
    } catch (URISyntaxException e) {
      throw sneakyThrow(e);
    }
  }

  private void addAuthorizationHeader(HttpGet httpRequest, String authToken) {
    if (StringUtils.isNotBlank(authToken)) {
      httpRequest.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + authToken);
    }
  }
}