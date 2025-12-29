package dev.streamx.cli.command.ingestion.batch;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.matching.EqualToJsonPattern;
import com.github.tomakehurst.wiremock.matching.MatchResult;

class CloudEventJsonMatcher extends EqualToJsonPattern {

  CloudEventJsonMatcher(@JsonProperty("equalToJson") String json) {
    super(maskIdAndTime(json), null, null);
  }

  private static String maskIdAndTime(String cloudEventJson) {
    return cloudEventJson
        .replaceFirst("\"id\" *: *\"[^\"]+\"", "\"id\" : \"[MASKED]\"")
        .replaceFirst("\"time\" *: *\"[^\"]+\"", "\"time\" : \"[MASKED]\"");
  }

  @Override
  public MatchResult match(String value) {
    return super.match(maskIdAndTime(value));
  }
}
