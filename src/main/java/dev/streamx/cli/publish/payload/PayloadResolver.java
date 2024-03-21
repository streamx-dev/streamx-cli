package dev.streamx.cli.publish.payload;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.InvalidJsonException;
import com.jayway.jsonpath.InvalidPathException;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import dev.streamx.cli.exception.ValueException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

@ApplicationScoped
public class PayloadResolver {

  private static final ObjectMapper objectMapper = new ObjectMapper();
  static {
    objectMapper.enable(Feature.ALLOW_SINGLE_QUOTES);
  }
  private final JsonProvider jsonProvider;
  private final MappingProvider mappingProvider;

  @Inject
  ValueReplacementExtractor valueReplacementExtractor;

  PayloadResolver() {
    jsonProvider = new JacksonJsonNodeJsonProvider(objectMapper);
    mappingProvider = new JacksonMappingProvider(objectMapper);
    configureDefaults();
  }

  public JsonNode createPayload(String data) {
    return createPayload(data, List.of());
  }

  public JsonNode createPayload(String data, List<String> values) {
    try {
      JsonNode payload = doCreatePayload(data, values);
      return payload;
    } catch (IOException e) {
      throw PayloadException.ioException(e);
    }
  }

  private JsonNode doCreatePayload(String data, List<String> values) throws IOException {
    DocumentContext documentContext = prepareWrappedJsonNode(
        data,
        (exception, source) -> { throw PayloadException.jsonParseException(exception, source); },
        (exception, source) -> { throw PayloadException.genericJsonProcessingException(exception, source); }
    );

    replaceValues(documentContext, values);

    return documentContext.json();
  }

  private static DocumentContext prepareWrappedJsonNode(String data,
      BiConsumer<JsonParseException, String> onJsonParseException,
      BiConsumer<JsonProcessingException, String> onJsonProcessingException
      ) {
    String source = null;
    try {
      source = readContent(data);

      String nullSafeSource = Optional.ofNullable(source)
          .filter(StringUtils::isNotEmpty)
          .orElse(" ");

      return JsonPath.parse(nullSafeSource);
    } catch (InvalidJsonException e) {
      Throwable cause = e.getCause();
      if (cause instanceof JsonParseException exception) {
        onJsonParseException.accept(exception, source);
      } else if (cause instanceof JsonProcessingException exception) {
        onJsonProcessingException.accept(exception, source);
      }
      throw PayloadException.ioException(e);
    }
  }

  private void replaceValues(DocumentContext documentContext, List<String> values) {
    if (values == null || values.isEmpty()) {
      return;
    }

    for (String value: values) {
      Pair<JsonPath, String> extract = valueReplacementExtractor.extract(value);

      JsonPath jsonPath = extract.getKey();
      DocumentContext replacement = prepareWrappedJsonNode(
          extract.getValue(),
          (exception, source) -> { throw ValueException.jsonParseException(exception, jsonPath, source); },
          (exception, source) -> { throw ValueException.genericJsonProcessingException(exception, jsonPath, source); }
      );

      try {
        documentContext = documentContext.set(jsonPath, replacement.json());
      } catch (PathNotFoundException e) {
        throw ValueException.pathNotFoundException(jsonPath);
      } catch (IllegalArgumentException | InvalidPathException e) {
        throw ValueException.genericJsonProcessingException(e, jsonPath, documentContext.jsonString());
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
    Path path = Path.of(data.substring(1));
    try {
      return Files.readString(path);
    } catch (NoSuchFileException e) {
      throw PayloadException.noSuchFileException(e, path);
    } catch (IOException e) {
      throw PayloadException.fileReadingException(e, path);
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
