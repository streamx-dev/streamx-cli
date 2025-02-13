package dev.streamx.cli.command.ingestion.batch.resolver.substitutor;

import static java.util.Objects.requireNonNull;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.Map;

@ApplicationScoped
public class StringSubstitutor implements Substitutor {

  @Override
  public Map<String, String> createSubstitutionVariables(String payloadPath,
      String channel, String relativePath) {
    return Map.of(
        "payloadPath", requireNonNull(payloadPath),
        "channel", requireNonNull(channel),
        "relativePath", requireNonNull(relativePath)
    );
  }

  @Override
  public String substitute(Map<String, String> variables, String text) {
    String result = text;
    for (Map.Entry<String, String> entry : variables.entrySet()) {
      // FixMe this should be a better solution
      result = result.replace("${" + entry.getKey() + "}", entry.getValue());
    }
    return result;
  }
}
