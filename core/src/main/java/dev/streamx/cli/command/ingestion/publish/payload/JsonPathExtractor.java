package dev.streamx.cli.command.ingestion.publish.payload;

import com.jayway.jsonpath.InvalidPathException;
import com.jayway.jsonpath.JsonPath;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;
import org.apache.commons.lang3.tuple.Pair;

@ApplicationScoped
class JsonPathExtractor {

  Optional<Pair<JsonPath, String>> extract(String valueArg) {
    for (int idx = valueArg.indexOf("=");
        idx != -1;
        idx = valueArg.indexOf("=", idx + "=".length())) {
      try {
        JsonPath jsonPath = JsonPath.compile(valueArg.substring(0, idx));
        String replacement = valueArg.substring(idx + "=".length());

        return Optional.of(Pair.of(jsonPath, replacement));
      } catch (InvalidPathException | IllegalArgumentException e) {
        // this probably means that '=' was part of JSONPath expression
        continue;
      }
    }

    return Optional.empty();
  }
}
