package dev.streamx.cli.ingestion.publish.payload;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.InvalidPathException;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import dev.streamx.cli.exception.PayloadException;
import dev.streamx.cli.exception.ValueException;
import dev.streamx.cli.ingestion.publish.PayloadArgument;
import dev.streamx.cli.ingestion.publish.payload.InitialPayloadResolver.InitialPayload;
import dev.streamx.cli.ingestion.publish.payload.source.RawPayload;
import dev.streamx.cli.ingestion.publish.payload.source.SourceResolver;
import dev.streamx.cli.ingestion.publish.payload.typed.SourceType;
import dev.streamx.cli.ingestion.publish.payload.typed.TypedPayloadFragmentResolver;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.IOException;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;

@ApplicationScoped
public class PayloadResolver {

  @Inject
  InitialPayloadResolver initialPayloadResolver;

  @Inject
  SourceResolver sourceResolver;

  @Inject
  TypedPayloadFragmentResolver typedPayloadFragmentResolver;

  @Inject
  ValueReplacementExtractor valueReplacementExtractor;

  public JsonNode createPayload(List<PayloadArgument> payloadArguments) {
    try {
      return doCreatePayload(payloadArguments);
    } catch (IOException e) {
      throw PayloadException.ioException(e);
    }
  }

  private JsonNode doCreatePayload(List<PayloadArgument> payloadArguments)
      throws IOException {
    InitialPayload result = initialPayloadResolver.computeInitialPayload(payloadArguments);

    String initialData = result.initialData();
    DocumentContext documentContext = prepareInitialDocument(initialData);

    replaceValues(documentContext, result.replacements());

    return documentContext.json();
  }

  private DocumentContext prepareInitialDocument(String initialData) {
    try {
      JsonNode initialJson = extractPayloadFragment(
          PayloadArgument.ofJsonNode(initialData), initialData);

      return JsonPath.parse(initialJson);
    } catch (JsonParseException exception) {
      throw PayloadException.jsonParseException(exception, initialData);
    } catch (JsonProcessingException exception) {
      throw PayloadException.genericJsonProcessingException(exception, initialData);
    } catch (IOException e) {
      throw PayloadException.ioException(e);
    }
  }

  private void replaceValues(DocumentContext documentContext,
      List<PayloadArgument> payloadArguments) {
    if (payloadArguments == null || payloadArguments.isEmpty()) {
      return;
    }

    for (PayloadArgument payloadArgument : payloadArguments) {
      String value = payloadArgument.getValue();
      Pair<JsonPath, String> extract = valueReplacementExtractor.extract(value)
          .orElseThrow(() -> ValueException.noJsonPathFoundException(value));

      JsonPath jsonPath = extract.getKey();
      JsonNode replacement;
      String extractValue = extract.getValue();

      try {
        replacement = extractPayloadFragment(payloadArgument, extractValue);
      } catch (JsonParseException exception) {
        throw ValueException.jsonParseException(exception, jsonPath, extractValue);
      } catch (JsonProcessingException exception) {
        throw ValueException.genericJsonProcessingException(exception, jsonPath, extractValue);
      } catch (IOException e) {
        throw PayloadException.ioException(e);
      }

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

  private JsonNode extractPayloadFragment(PayloadArgument payloadArgument, String value)
      throws IOException {
    RawPayload rawPayload = sourceResolver.resolve(value);
    SourceType sourceType = SourceType.STRING;
    if (payloadArgument.isBinary()) {
      sourceType = SourceType.BINARY;
    }
    if (payloadArgument.isJson()) {
      sourceType = SourceType.JSON;
    }

    return typedPayloadFragmentResolver.resolveFragment(rawPayload, sourceType).jsonNode();
  }
}
