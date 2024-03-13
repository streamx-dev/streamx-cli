package dev.streamx.cli.publish;

import static dev.streamx.cli.util.ExceptionUtils.sneakyThrow;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.ParseResult;

@ApplicationScoped
class SchemaProvider {

  @Inject
  CloseableHttpClient httpClient;

  @Inject
  CommandLine.ParseResult parseResult;

  JsonNode provideSchema(String channel) {
    String ingestionUrl = getIngestionUrl();
    Map<String, JsonNode> schemas = fetchSchema(ingestionUrl);

    validateChannel(channel, schemas);

    return schemas.get(channel);
  }

  private void validateChannel(String channel, Map<String, JsonNode> stringJsonNodeMap) {
    if (!stringJsonNodeMap.containsKey(channel)) {
      String availableChannels = stringJsonNodeMap.keySet().stream()
          .sorted()
          .collect(Collectors.joining(", "));

      throw new ParameterException(parseResult.commandSpec().commandLine(),
          "Channel '" + channel + "' not found. "
          + "Available channels: " + availableChannels);
    }
  }

  @NotNull
  private String getIngestionUrl() {
    return Optional.ofNullable(parseResult.matchedOption("--ingestion-url"))
        .or(() -> Optional.ofNullable(parseResult)
            .filter(ParseResult::hasSubcommand)
            .map(ParseResult::subcommand)
            .map(pr -> pr.matchedOption("--ingestion-url")))
        .map(option -> option.stringValues().get(0))
        .orElseThrow();
  }

  private Map<String, JsonNode> fetchSchema(String ingestionUrl) {
    try {
      URI publicationEndpointUri = buildPublicationsUri(ingestionUrl);
      HttpGet httpRequest = new HttpGet(publicationEndpointUri);
      HttpResponse execute = httpClient.execute(httpRequest);
      HttpEntity entity = execute.getEntity();

      String body = EntityUtils.toString(entity, "UTF-8");

      ObjectMapper objectMapper = new ObjectMapper();
      return objectMapper.readValue(body, new TypeReference<>() {});
    } catch (IOException e) {
      throw sneakyThrow(e); // FIXME
//      throw new StreamxClientException(
//          String.format("PUT request with URI: %s failed due to HTTP client error", endpointUri),
//          e);
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
