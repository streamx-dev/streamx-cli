package dev.streamx.cli.ingestion;

import static dev.streamx.cli.util.ExceptionUtils.sneakyThrow;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.streamx.cli.exception.UnableToConnectIngestionServiceException;
import dev.streamx.cli.exception.UnknownChannelException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

@ApplicationScoped
public class SchemaProvider {

  @Inject
  CloseableHttpClient httpClient;

  @Inject
  IngestionClientConfig ingestionClientConfig;

  public void validateChannel(String channel) {
    String ingestionUrl = ingestionClientConfig.url();
    Map<String, JsonNode> schemas = fetchSchema(ingestionUrl);

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

  private Map<String, JsonNode> fetchSchema(String ingestionUrl) {
    try {
      URI publicationEndpointUri = buildPublicationsUri(ingestionUrl);
      HttpGet httpRequest = new HttpGet(publicationEndpointUri);

      ingestionClientConfig.authToken()
          .ifPresent(authToken -> addAuthorizationHeader(httpRequest, authToken));

      HttpResponse execute = httpClient.execute(httpRequest);
      HttpEntity entity = execute.getEntity();

      String body = EntityUtils.toString(entity, "UTF-8");

      ObjectMapper objectMapper = new ObjectMapper();
      return objectMapper.readValue(body, new TypeReference<>() {
      });
    } catch (ConnectException e) {
      throw new UnableToConnectIngestionServiceException(ingestionUrl);
    } catch (IOException e) {
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

  private void addAuthorizationHeader(HttpGet httpRequest, String authToken) {
    if (StringUtils.isNotBlank(authToken)) {
      httpRequest.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + authToken);
    }
  }
}
