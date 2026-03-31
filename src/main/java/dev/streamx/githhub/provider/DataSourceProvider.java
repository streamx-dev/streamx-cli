package dev.streamx.githhub.provider;

import static dev.streamx.githhub.Constants.INGESTION_INCLUDE_PATTERNS;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.streamx.exception.GitHubActionException;
import dev.streamx.githhub.Constants;
import io.cloudevents.CloudEvent;
import io.quarkiverse.githubaction.Context;
import io.quarkiverse.githubaction.Inputs;
import java.util.List;
import java.util.Optional;
import org.kohsuke.github.GHEventPayload;

public interface DataSourceProvider {

  String getName();

  ObjectMapper getMapper();

  List<CloudEvent> createPayload(Inputs inputs, Context context, GHEventPayload payload)
      throws GitHubActionException;

  default String getWorkspace(Inputs inputs, String defaultWorkspace) {
    return Optional.ofNullable(getInputString(inputs, Constants.INGESTION_WORKSPACE))
        .orElse(defaultWorkspace);
  }

  default String getInputString(Inputs inputs, String inputParam) {
    return Optional.ofNullable(inputs)
        .map(i -> i.get(inputParam))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .orElse(null);
  }

  default String[] getIncludePatterns(Inputs inputs) throws JsonProcessingException {
    Optional<String> filePatternsInputOpt = inputs.get(INGESTION_INCLUDE_PATTERNS);
    if (filePatternsInputOpt.isPresent()) {
      return getMapper().readValue(filePatternsInputOpt.get(), String[].class);
    }
    return null;
  }

}
