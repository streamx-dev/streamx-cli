package dev.streamx.cli.publish.payload;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.InvalidJsonException;
import com.jayway.jsonpath.InvalidPathException;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;

@ApplicationScoped
public class PayloadResolver {

  private final JsonProvider jsonProvider;
  private final MappingProvider mappingProvider;

  @Inject
  ValueReplacementExtractor valueReplacementExtractor;

  PayloadResolver() {
    jsonProvider = new JacksonJsonNodeJsonProvider();
    mappingProvider = new JacksonMappingProvider();
    configureDefaults();
  }

  public JsonNode createPayload(String data) {
    return createPayload(data, List.of());
  }

  public JsonNode createPayload(String data, List<String> values) {
    try {
      return doCreatePayload(data, values);
    } catch (InvalidJsonException | JsonProcessingException e) {
      throw PayloadException.jsonProcessingException(e);
    } catch (IOException e) {
      throw PayloadException.ioException(e);
    }
  }

  private JsonNode doCreatePayload(String data, List<String> values) throws IOException {
    DocumentContext documentContext = prepareJsonPayloadSource(data);

    replaceValues(documentContext, values);

    return documentContext.json();
  }

  private static DocumentContext prepareJsonPayloadSource(String data) {
    String payload = readContent(data);

    return JsonPath.parse(payload);
  }

  private void replaceValues(DocumentContext documentContext, List<String> values) {
    if (values == null || values.isEmpty()) {
      return;
    }

    for (String value: values) {
      Pair<JsonPath, String> extract = valueReplacementExtractor.extract(value);
      try {
        documentContext = documentContext.set(extract.getKey(), extract.getValue());
      } catch (IllegalArgumentException | InvalidPathException e) {
        throw PayloadException.jsonProcessingException(e);
      }
    }
  }

  private static String readContent(String data) {
    if (data.startsWith("@")) {
      return readPayloadFromFile(data);
    } else {
      return data;
    }
  }

  private static String readPayloadFromFile(String data) {
    try {
      Path path = Path.of(data.substring(1));

      return Files.readString(path);
    } catch (IOException e) {
      throw PayloadException.fileReadingException(e);
    }
  }

  private void configureDefaults() {
    Configuration.setDefaults(new Configuration.Defaults() {
      @Override
      public JsonProvider jsonProvider() {
        return jsonProvider;
      }

      @Override
      public Set<Option> options() {
        return Set.of();
      }

      @Override
      public MappingProvider mappingProvider() {
        return mappingProvider;
      }
    });
  }
}
