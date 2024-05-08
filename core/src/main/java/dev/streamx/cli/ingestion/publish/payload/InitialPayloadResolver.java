package dev.streamx.cli.ingestion.publish.payload;

import dev.streamx.cli.exception.PayloadException;
import dev.streamx.cli.ingestion.publish.PayloadArgument;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

@ApplicationScoped
class InitialPayloadResolver {

  @Inject
  ValueReplacementExtractor valueReplacementExtractor;

  @NotNull
  InitialPayload computeInitialPayload(List<PayloadArgument> payloadArguments) {
    String initialPayload = "{}";
    List<PayloadArgument> replacements = payloadArguments;

    String firstPayloadArgValue = payloadArguments.stream()
        .map(PayloadArgument::getValue)
        .findFirst()
        .orElseThrow(PayloadException::payloadNotFound);

    boolean firstDataIsInitialPayload = valueReplacementExtractor.extract(firstPayloadArgValue)
        .isEmpty();
    if (firstDataIsInitialPayload) {
      initialPayload = firstPayloadArgValue;
      replacements = payloadArguments.subList(1, payloadArguments.size());
    }

    return new InitialPayload(initialPayload, replacements);
  }

  record InitialPayload(String initialData, List<PayloadArgument> replacements) {

  }
}
