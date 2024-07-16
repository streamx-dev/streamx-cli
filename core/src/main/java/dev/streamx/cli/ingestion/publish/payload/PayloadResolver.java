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

  /**
   * <p>Payload is created by merging all payload fragments derived from payload arguments
   * into single Json. The creation of payload fragment from each payload argument is done
   * in three steps:
   * <ol>
   *   <li>Finding and filter out JSONPath expression to replace with value e.g.
   *   {@code content.bytes=file:///text.txt }</li>
   *   <li>Resolving content of json node e.g. resolving {@code file:///text.txt }</li>
   *   <li>Recreating full payload fragment from JSONPath expression (if passed)
   *   or creating json from resolved content</li>
   * </ol>
   * </p>
   * <p>All payload fragments are merged into single json. E.g.
   * {@code -j content.num=123 -j {"content":{"text":"text"}}}
   * results in {@code {"content":{"text":"text", "num":123}}}
   * </p>
   * @param payloadArguments List of arguments creating payload fragments
   * @return Merged json created from payload arguments
   */
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
      String rawPayloadArgumentValue = payloadArgument.getValue();
      Pair<JsonPath, String> extract = extractJsonPathReplacement(
          payloadArgument, rawPayloadArgumentValue);

      JsonPath jsonPath = extract.getKey();
      String replacementSource = extract.getValue();
      SourceType sourceType = payloadArgument.getSourceType();
      JsonNode replacement = extractPayloadFragment(sourceType, replacementSource, jsonPath);

      documentContext = merge(documentContext, jsonPath, replacement, rawPayloadArgumentValue);
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

  private JsonNode extractPayloadFragment(SourceType sourceType, String replacementSource,
      JsonPath jsonPath) {
    try {
      return extractPayloadFragment(sourceType, replacementSource);
    } catch (JsonParseException exception) {
      if (jsonPath != null) {
        throw JsonPathReplacementException.jsonParseException(exception, jsonPath,
            replacementSource);
      } else {
        throw PayloadException.jsonParseException(exception, replacementSource);
      }
    } catch (JsonProcessingException exception) {
      throw JsonPathReplacementException.genericJsonProcessingException(exception, jsonPath,
          replacementSource);
    } catch (IOException e) {
      throw PayloadException.ioException(e);
    }
  }

  private JsonNode extractPayloadFragment(SourceType sourceType, String replacementSource)
      throws IOException {
    RawPayload rawPayload = sourceResolver.resolve(replacementSource);

    return typedPayloadFragmentResolver.resolveFragment(rawPayload, sourceType).jsonNode();
  }
}
