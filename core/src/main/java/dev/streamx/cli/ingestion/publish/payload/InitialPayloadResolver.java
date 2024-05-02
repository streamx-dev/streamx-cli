package dev.streamx.cli.ingestion.publish.payload;

import dev.streamx.cli.exception.PayloadException;
import dev.streamx.cli.ingestion.publish.DataArguments;
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
  InitialPayload computeInitialPayload(List<DataArguments> dataArgs) {
    String initialData = "{}";
    List<DataArguments> replacements = dataArgs;

    String data = dataArgs.stream()
        .map(DataArguments::getValue)
        .findFirst()
        .orElseThrow(PayloadException::payloadNotFound);

    boolean firstDataIsInitialPayload = valueReplacementExtractor.extract(data)
        .map(Pair::getLeft)
        .isEmpty();
    if (firstDataIsInitialPayload) {
      initialData = data;
      replacements = dataArgs.subList(1, dataArgs.size());
    }

    return new InitialPayload(initialData, replacements);
  }

  record InitialPayload(String initialData, List<DataArguments> replacements) {

  }
}
