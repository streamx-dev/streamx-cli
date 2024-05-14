package dev.streamx.cli.ingestion.publish.payload.source;

import jakarta.enterprise.context.ApplicationScoped;
import java.nio.charset.StandardCharsets;

@ApplicationScoped
public class ExactSourceResolver {

  RawPayload resolve(String rawSource) {
    byte[] result = rawSource.getBytes(StandardCharsets.UTF_8);
    return new RawPayload(result);
  }
}
