package dev.streamx.cli.ingestion.publish.payload.source;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class SourceResolver {

  @Inject
  FileSourceResolver fileSourceResolver;

  @Inject
  ExactSourceResolver exactSourceResolver;

  public RawPayload resolve(String rawSource) {
    if (fileSourceResolver.applies(rawSource)) {
      return fileSourceResolver.resolve(rawSource);
    }

    return exactSourceResolver.resolve(rawSource);
  }
}
