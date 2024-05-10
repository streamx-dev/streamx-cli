package dev.streamx.cli.ingestion.publish.payload;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import dev.streamx.cli.exception.JsonPathReplacementException;
import dev.streamx.cli.exception.PayloadException;
import dev.streamx.cli.ingestion.PayloadProcessing;
import dev.streamx.cli.ingestion.publish.PayloadArgument;
import dev.streamx.cli.ingestion.publish.payload.source.RawPayload;
import dev.streamx.cli.ingestion.publish.payload.source.SourceResolver;
import dev.streamx.cli.ingestion.publish.payload.typed.SourceType;
import dev.streamx.cli.ingestion.publish.payload.typed.TypedPayloadFragmentResolver;
import dev.streamx.cli.util.CollectionUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

@ApplicationScoped
public class PayloadResolver {

  @Inject
  SourceResolver sourceResolver;

  @Inject
  TypedPayloadFragmentResolver typedPayloadFragmentResolver;

  @Inject
  JsonPathExtractor jsonPathExtractor;

  @Inject
  @PayloadProcessing
  ObjectMapper objectMapper;

  public JsonNode createPayload(List<PayloadArgument> payloadArguments) {
    if (CollectionUtils.isEmpty(payloadArguments)) {
      throw PayloadException.payloadNotFound();
    }

    DocumentContext documentContext = prepareInitialDocument();

    documentContext = mergePayloads(documentContext, payloadArguments);

    return documentContext.json();
  }

  private DocumentContext prepareInitialDocument() {
    JsonNode initialJson = objectMapper.createObjectNode();

    return JsonPath.parse(initialJson);
  }

  private DocumentContext mergePayloads(DocumentContext documentContext,
      List<PayloadArgument> payloadArguments) {

    for (PayloadArgument payloadArgument : payloadArguments) {
      String value = payloadArgument.getValue();
      Pair<JsonPath, String> extract = extractJsonPathReplacement(payloadArgument, value);

      JsonPath jsonPath = extract.getKey();
      SourceType sourceType = payloadArgument.getSourceType();
      JsonNode replacement = extractPayloadFragment(sourceType, extract.getValue(), jsonPath);

      documentContext = merge(documentContext, jsonPath, replacement, value);
    }
    return documentContext;
  }

  @NotNull
  private Pair<JsonPath, String> extractJsonPathReplacement(PayloadArgument payloadArgument,
      String value) {
    Optional<Pair<JsonPath, String>> jsonPathReplacement = jsonPathExtractor.extract(value);
    if (!payloadArgument.getSourceType().isMergeAllowed()
        && jsonPathReplacement.isEmpty()) {
      throw JsonPathReplacementException.noJsonPathFoundException(value);
    }

    return jsonPathReplacement.orElse(Pair.of(null, value));
  }

  private DocumentContext merge(DocumentContext documentContext, JsonPath jsonPath,
      JsonNode replacement, String value) {
    if (jsonPath != null) {
      documentContext = mergeJsonPath(documentContext, jsonPath, replacement);
    } else {
      documentContext = mergeRoot(documentContext, replacement, value);
    }
    return documentContext;
  }

  private DocumentContext mergeRoot(DocumentContext documentContext, JsonNode replacement,
      String value) {
    JsonNode currentJsonNode = documentContext.json();
    JsonNode merged;
    try {
      merged = mergeNewValueWithOldValue(currentJsonNode, replacement);
    } catch (JsonProcessingException e) {
      throw PayloadException.genericJsonProcessingException(e, value);
    }

    documentContext = JsonPath.parse(merged);
    return documentContext;
  }

  private DocumentContext mergeJsonPath(DocumentContext documentContext, JsonPath jsonPath,
      JsonNode replacement) {
    try {
      JsonNode currentJsonNode = documentContext.read(jsonPath);
      replacement = mergeNewValueWithOldValue(currentJsonNode, replacement);
    } catch (JsonProcessingException e) {
      throw PayloadException.genericJsonProcessingException(e, replacement.toString());
    }

    try {
      documentContext = documentContext.set(jsonPath, replacement);
    } catch (PathNotFoundException e) {
      throw JsonPathReplacementException.pathNotFoundException(jsonPath);
    }
    return documentContext;
  }

  private JsonNode mergeNewValueWithOldValue(JsonNode jsonNode, JsonNode replacement)
      throws JsonProcessingException {
    if (replacement.isObject() && jsonNode != null) {
      return objectMapper
          .readerForUpdating(jsonNode)
          .readTree(replacement.toString());
    }
    return replacement;
  }

  private JsonNode extractPayloadFragment(SourceType sourceType, String rawReplacement,
      JsonPath jsonPath) {
    try {
      return extractPayloadFragment(sourceType, rawReplacement);
    } catch (JsonParseException exception) {
      if (jsonPath != null) {
        throw JsonPathReplacementException.jsonParseException(exception, jsonPath, rawReplacement);
      } else {
        throw PayloadException.jsonParseException(exception, rawReplacement);
      }
    } catch (JsonProcessingException exception) {
      throw JsonPathReplacementException.genericJsonProcessingException(exception, jsonPath,
          rawReplacement);
    } catch (IOException e) {
      throw PayloadException.ioException(e);
    }
  }

  private JsonNode extractPayloadFragment(SourceType sourceType, String rawSource)
      throws IOException {
    RawPayload rawPayload = sourceResolver.resolve(rawSource);

    return typedPayloadFragmentResolver.resolveFragment(rawPayload, sourceType).jsonNode();
  }
}
