package dev.streamx.cli.command.ingestion.publish.payload.source;

import dev.streamx.cli.exception.PayloadException;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

@ApplicationScoped
public class FileSourceResolver {

  public static final String FILE_STRATEGY_PREFIX = "file://";

  boolean applies(String rawSource) {
    return rawSource != null && rawSource.startsWith(FILE_STRATEGY_PREFIX);
  }

  RawPayload resolve(String rawSource) {
    String source = rawSource.substring(FILE_STRATEGY_PREFIX.length());

    byte[] result = readFile(source);
    return new RawPayload(result);
  }

  private static byte[] readFile(String data) {
    Path path = Path.of(data);
    try {
      return Files.readAllBytes(path);
    } catch (NoSuchFileException e) {
      throw PayloadException.noSuchFileException(e, path);
    } catch (IOException e) {
      throw PayloadException.fileReadingException(e, path);
    }
  }
}
