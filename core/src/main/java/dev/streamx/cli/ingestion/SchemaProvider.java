package dev.streamx.cli.ingestion;

import static dev.streamx.cli.util.ExceptionUtils.sneakyThrow;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.streamx.cli.exception.UnableToConnectIngestionServiceException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

@ApplicationScoped
public class SchemaProvider {

  @Inject
  CloseableHttpClient httpClient;

  @Inject
  IngestionClientContext ingestionClientContext;

  public List<String> getValidChannels() {
    String ingestionUrl = ingestionClientContext.getIngestionUrl();
    return fetchChannels(ingestionUrl)
        .stream()
        .sorted()
        .toList();
  }

  private Set<String> fetchChannels(String ingestionUrl) {
    try {
      URI publicationEndpointUri = buildPublicationsUri(ingestionUrl);
      HttpGet httpRequest = new HttpGet(publicationEndpointUri);
      HttpResponse execute = httpClient.execute(httpRequest);
      HttpEntity entity = execute.getEntity();

      String body = EntityUtils.toString(entity, "UTF-8");

      ObjectMapper objectMapper = new ObjectMapper();
      Map<String, JsonNode> data = objectMapper.readValue(body, new TypeReference<>() {
      });
      return data.keySet();
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
}
