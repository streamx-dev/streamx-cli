package dev.streamx.ingestion.payload;

import dev.streamx.exception.GitHubActionException;
import dev.streamx.ingestion.IngestionPayload;
import io.cloudevents.CloudEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;

public class FilePayload implements IngestionPayload {

  private static final Logger log = Logger.getLogger(FilePayload.class);

  private final String eventType;
  private final String workspace;
  private final String filePath;
  private final String absolutePath;

  public FilePayload(String eventType, String workspace, String filePath) {
    this.eventType = eventType;
    this.workspace = workspace;
    this.filePath = filePath;
    this.absolutePath = resolveAbsolutPath();
  }

  private String resolveAbsolutPath() {
    return StringUtils.startsWith(filePath, File.separator)
        ? workspace + filePath :
        workspace + File.separator + filePath;
  }

  @Override
  public CloudEvent resolve() throws GitHubActionException {
    byte[] bytes = readBytes(absolutePath);
    if (log.isDebugEnabled()) {
      log.debugf("Read file: %s, bytes length: %d", absolutePath, bytes.length);
    }
    CloudEvent event = CloudEventFactory.createPublishEvent(eventType, filePath, bytes);
    if (log.isDebugEnabled()) {
      log.debugf("CloudEvent: type=%s, subject=%s", event.getType(), event.getSubject());
    }
    return event;
  }

  private byte[] readBytes(String data) throws GitHubActionException {
    Path path = Path.of(data);
    try {
      return Files.readAllBytes(path);
    } catch (NoSuchFileException e) {
      throw new GitHubActionException(String.format("Can not read file %s: %s",
          data, e.getMessage()), e);
    } catch (IOException e) {
      throw new GitHubActionException(String.format("Failed to read file %s: %s",
          data, e.getMessage()), e);
    }
  }

}
