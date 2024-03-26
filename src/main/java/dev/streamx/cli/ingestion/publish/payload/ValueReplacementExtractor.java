package dev.streamx.cli.ingestion.publish.payload;

import com.jayway.jsonpath.InvalidPathException;
import com.jayway.jsonpath.JsonPath;
import dev.streamx.cli.exception.ValueException;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.commons.lang3.tuple.Pair;

@ApplicationScoped
class ValueReplacementExtractor {

  Pair<JsonPath, String> extract(String valueArg) {
    for (int idx = valueArg.indexOf("=");
        idx != -1;
        idx = valueArg.indexOf("=", idx + "=".length())) {
      try {
        JsonPath jsonPath = JsonPath.compile(valueArg.substring(0, idx));
        String replacement = valueArg.substring(idx + "=".length());

        return Pair.of(jsonPath, replacement);
      } catch (InvalidPathException | IllegalArgumentException e) {
        // this probably means that '=' was part of jsonPath
        continue;
      }
    }

    throw ValueException.noJsonPathFoundException(valueArg);
  }
}
