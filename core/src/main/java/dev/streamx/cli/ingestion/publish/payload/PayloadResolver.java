package dev.streamx.cli.ingestion.publish.payload;

import static dev.streamx.cli.ingestion.publish.payload.PayloadResolverUtils.prepareWrappedJsonNode;
import static dev.streamx.cli.ingestion.publish.payload.PayloadResolverUtils.readContent;
import static dev.streamx.cli.ingestion.publish.payload.PayloadResolverUtils.readStringContent;

import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.InvalidPathException;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import dev.streamx.cli.exception.PayloadException;
import dev.streamx.cli.exception.ValueException;
import dev.streamx.cli.ingestion.publish.DataArguments;
import dev.streamx.cli.ingestion.publish.payload.InitialPayloadResolver.InitialPayload;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import org.apache.avro.util.internal.JacksonUtils;
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
  InitialPayloadResolver initialPayloadResolver;

  @Inject
  ValueReplacementExtractor valueReplacementExtractor;

  PayloadResolver() {
    jsonProvider = new PropertyCreatingJacksonJsonNodeJsonProvider(objectMapper);
    mappingProvider = new JacksonMappingProvider(objectMapper);
    configureDefaults();
  }

  public JsonNode createPayload(String data) {
    return createPayload(List.of(DataArguments.of(data)));
  }

  public JsonNode createPayload(List<DataArguments> dataArgs) {
    try {
      return doCreatePayload(dataArgs);
    } catch (IOException e) {
      throw PayloadException.ioException(e);
    }
  }

  private JsonNode doCreatePayload(List<DataArguments> dataArgs)
      throws IOException {
    InitialPayload result = initialPayloadResolver.computeInitialPayload(dataArgs);

    DocumentContext documentContext = prepareWrappedJsonNode(
        result.initialData(),
        (exception, source) -> {
          throw PayloadException.jsonParseException(exception, source);
        },
        (exception, source) -> {
          throw PayloadException.genericJsonProcessingException(exception, source);
        }
    );

    replaceValues(documentContext, result.replacements());

    return documentContext.json();
  }

  private void replaceValues(DocumentContext documentContext, List<DataArguments> dataArguments) {
    if (dataArguments == null || dataArguments.isEmpty()) {
      return;
    }

    for (DataArguments valueArgument : dataArguments) {
      String value = valueArgument.getValue();
      Pair<JsonPath, String> extract = valueReplacementExtractor.extract(value)
          .orElseThrow(() -> ValueException.noJsonPathFoundException(value));

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

  private static JsonNode extractReplacement(DataArguments valueArgument,
      Pair<JsonPath, String> extract) {
    JsonPath jsonPath = extract.getKey();
    String value = extract.getValue();

    if (valueArgument.isJson()) {
      return prepareWrappedJsonNode(
          value,
          (exception, source) -> {
            throw ValueException.jsonParseException(exception, jsonPath, source);
          },
          (exception, source) -> {
            throw ValueException.genericJsonProcessingException(exception, jsonPath, source);
          }
      ).json();
    } else if (valueArgument.isBinary()) {
      byte[] bytes = readContent(value);

      return JacksonUtils.toJsonNode(bytes);
    } else {
      String content = readStringContent(value);

      return TextNode.valueOf(content);
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
