package dev.streamx.cli.ingestion.publish.payload;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
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
import dev.streamx.cli.ingestion.publish.ValueArguments;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import org.apache.avro.util.internal.JacksonUtils;
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

  public JsonNode createPayload(String data, List<ValueArguments> values) {
    try {
      return doCreatePayload(data, values);
    } catch (IOException e) {
      throw PayloadException.ioException(e);
    }
  }

  private JsonNode doCreatePayload(String data, List<ValueArguments> values) throws IOException {
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
      source = readStringContent(data);

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

  private void replaceValues(DocumentContext documentContext, List<ValueArguments> valueArguments) {
    if (valueArguments == null || valueArguments.isEmpty()) {
      return;
    }

    for (ValueArguments valueArgument: valueArguments) {
      Pair<JsonPath, String> extract = valueReplacementExtractor.extract(valueArgument.getValue());

      JsonPath jsonPath = extract.getKey();
      JsonNode replacement = extractReplacement(valueArgument, extract, jsonPath);

      try {
        documentContext = documentContext.set(jsonPath, replacement);
      } catch (PathNotFoundException e) {
        throw ValueException.pathNotFoundException(jsonPath);
      } catch (IllegalArgumentException | InvalidPathException e) {
        throw ValueException.genericJsonProcessingException(e, jsonPath, documentContext.jsonString());
      }
    }
  }

  private static JsonNode extractReplacement(ValueArguments valueArgument, Pair<JsonPath, String> extract,
      JsonPath jsonPath) {
    String value = extract.getValue();

    if (valueArgument.isBinary()) {
      byte[] bytes = readContent(value);

      return JacksonUtils.toJsonNode(bytes);
    } else if (valueArgument.isString()) {
      String content = readStringContent(value);

      return TextNode.valueOf(content);
    } else {
      return prepareWrappedJsonNode(
          value,
          (exception, source) -> { throw ValueException.jsonParseException(exception, jsonPath, source); },
          (exception, source) -> { throw ValueException.genericJsonProcessingException(exception,
              jsonPath, source); }
      ).json();
    }
  }

  private static String readStringContent(String argument) {
    if (argument.startsWith("@")) {
      return new String(readFile(argument), StandardCharsets.UTF_8);
    } else {
      return argument;
    }
  }

  private static byte[] readContent(String data) {
    if (data.startsWith("@")) {
      return readFile(data);
    } else {
      return data.getBytes(StandardCharsets.UTF_8);
    }
  }

  private static byte[] readFile(String data) {
    Path path = Path.of(data.substring(1));
    try {
      return Files.readAllBytes(path);
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
