package dev.streamx.cli.ingestion;

import static dev.streamx.cli.util.ExceptionUtils.sneakyThrow;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.streamx.cli.exception.UnableToConnectIngestionServiceException;
import dev.streamx.cli.exception.UnknownChannelException;
import dev.streamx.clients.ingestion.exceptions.StreamxClientException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

@ApplicationScoped
public class SchemaProvider {

  @Inject
  CloseableHttpClient httpClient;

  @Inject
  IngestionClientContext ingestionClientContext;

  public void validateChannel(String channel) {
    Map<String, JsonNode> schemas = fetchSchema();

    validateChannel(channel, schemas);
  }

  private void validateChannel(String channel, Map<String, JsonNode> stringJsonNodeMap) {
    if (!stringJsonNodeMap.containsKey(channel)) {
      String availableChannels = stringJsonNodeMap.keySet().stream()
          .sorted()
          .collect(Collectors.joining(", "));

      throw new UnknownChannelException(channel, availableChannels);
    }
  }

  private Map<String, JsonNode> fetchSchema() {
    String ingestionUrl = ingestionClientContext.getIngestionUrl();

    try {
      URI publicationEndpointUri = buildPublicationsUri(ingestionUrl);
      HttpGet httpRequest = new HttpGet(publicationEndpointUri);

      String oauth2Bearer = ingestionClientContext.getOauth2Bearer();
      if (oauth2Bearer != null) {
        httpRequest.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + oauth2Bearer);
      }
      HttpResponse execute = httpClient.execute(httpRequest);
      HttpEntity entity = execute.getEntity();

      StatusLine statusLine = execute.getStatusLine();
      int statusCode = statusLine.getStatusCode();
      switch (statusCode) {
        case HttpStatus.SC_OK:
          String body = EntityUtils.toString(entity, "UTF-8");

          ObjectMapper objectMapper = new ObjectMapper();
          EntityUtils.consume(entity);
          return objectMapper.readValue(body, new TypeReference<>() {});
        case HttpStatus.SC_UNAUTHORIZED:
          throw new StreamxClientException(
              "Authentication failed. Make sure that the given token is valid.");
        default:
          throw new StreamxClientException(
              "Schema fetching failed");
      }
    } catch (ConnectException e) {
      throw new UnableToConnectIngestionServiceException(ingestionUrl);
    } catch (IOException | StreamxClientException e) {
      throw sneakyThrow(e);
    }
  }

  private URI buildPublicationsUri(String publicationsEndpointUri) {
    String uriString = String.format("%s/publications/v1/schema", publicationsEndpointUri);
    try {
      return new URI(uriString);
    } catch (URISyntaxException e) {
      throw sneakyThrow(e);
    }
  }
}
