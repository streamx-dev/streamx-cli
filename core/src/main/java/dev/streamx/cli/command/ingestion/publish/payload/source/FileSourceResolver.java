package dev.streamx.cli.command.ingestion.publish.payload.source;

import dev.streamx.cli.util.FileSourceUtils;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class FileSourceResolver {

  boolean applies(String rawSource) {
    return FileSourceUtils.applies(rawSource);
  }

  RawPayload resolve(String rawSource) {
    return new RawPayload(FileSourceUtils.resolve(rawSource));
  }
}
