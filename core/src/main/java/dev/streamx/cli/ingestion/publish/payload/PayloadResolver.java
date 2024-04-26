package dev.streamx.cli.ingestion.publish.payload;

import static dev.streamx.cli.ingestion.publish.payload.PayloadResolverUtils.readContent;
import static dev.streamx.cli.ingestion.publish.payload.PayloadResolverUtils.readStringContent;

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
import dev.streamx.cli.exception.PayloadException;
import dev.streamx.cli.exception.ValueException;
import dev.streamx.cli.ingestion.publish.ValueArguments;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import org.apache.avro.util.internal.JacksonUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

@ApplicationScoped
public class PayloadResolver {

  private static final String NULL_JSON_SOURCE = "null";

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
    return createPayload(null, data, List.of());
  }

  public JsonNode createPayload(String payloadArg, String data, List<ValueArguments> values) {
    try {
      return doCreatePayload(payloadArg, data, values);
    } catch (IOException e) {
      throw PayloadException.ioException(e);
    }
  }

  private JsonNode doCreatePayload(String payloadArg, String data, List<ValueArguments> values)
      throws IOException {
    DocumentContext documentContext = prepareWrappedJsonNode(
        payloadArg, data,
        (exception, source) -> {
          throw PayloadException.jsonParseException(exception, source);
        },
        (exception, source) -> {
          throw PayloadException.genericJsonProcessingException(exception, source);
        }
    );

    replaceValues(documentContext, values);

    return documentContext.json();
  }

  private static DocumentContext prepareWrappedJsonNode(String payloadArg, String data,
      BiConsumer<JsonParseException, String> onJsonParseException,
      BiConsumer<JsonProcessingException, String> onJsonProcessingException
  ) {
    String source = null;
    try {
      source = readStringContent(payloadArg, data);

      String nullSafeSource = Optional.of(source)
          .filter(StringUtils::isNotEmpty)
          .orElse(NULL_JSON_SOURCE);

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

    for (ValueArguments valueArgument : valueArguments) {
      Pair<JsonPath, String> extract = valueReplacementExtractor.extract(valueArgument.getValue());

      JsonPath jsonPath = extract.getKey();
      JsonNode replacement = extractReplacement(valueArgument, extract);

      try {
        documentContext = documentContext.set(jsonPath, replacement);
      } catch (PathNotFoundException e) {
        throw ValueException.pathNotFoundException(jsonPath);
      } catch (IllegalArgumentException | InvalidPathException e) {
        throw ValueException.genericJsonProcessingException(e, jsonPath,
            documentContext.jsonString());
      }
    }
  }

  private static JsonNode extractReplacement(ValueArguments valueArgument,
      Pair<JsonPath, String> extract) {
    JsonPath jsonPath = extract.getKey();
    String value = extract.getValue();

    if (valueArgument.isBinary()) {
      byte[] bytes = readContent(value);

      return JacksonUtils.toJsonNode(bytes);
    } else if (valueArgument.isString()) {
      String content = readStringContent(null, value);

      return TextNode.valueOf(content);
    } else {
      return prepareWrappedJsonNode(
          null,
          value,
          (exception, source) -> {
            throw ValueException.jsonParseException(exception, jsonPath, source);
          },
          (exception, source) -> {
            throw ValueException.genericJsonProcessingException(exception, jsonPath, source);
          }
      ).json();
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
