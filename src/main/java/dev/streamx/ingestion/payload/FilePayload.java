package dev.streamx.ingestion.payload;

import com.fasterxml.jackson.databind.JsonNode;
import dev.streamx.exception.GitHubActionException;
import dev.streamx.ingestion.IngestionPayloadJsonFactory;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;

public class FilePayload extends AbstractSchemaTypePayload {

  private static final Logger log = Logger.getLogger(FilePayload.class);

  private final String action;
  private final String workspace;
  private final String filePath;
  private final String absolutePath;

  public FilePayload(String action, String workspace, String filePath,
      String schemaType) {
    super(schemaType);
    this.action = action;
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
  public String getAction() {
    return action;
  }

  public JsonNode resolve() throws GitHubActionException {
    byte[] bytes = readBytes(absolutePath);
    if (log.isDebugEnabled()) {
      log.debugf("Read file: %s, bytes length: %d", absolutePath, bytes.length);
    }
    JsonNode bytesNode = toJsonNode(bytes);
    JsonNode message = IngestionPayloadJsonFactory.createMessage(
        filePath,
        getAction(),
        IngestionPayloadJsonFactory.createPayloadContent(bytesNode),
        getIngestionProperties(),
        getSchemaType()
    );
    if (log.isDebugEnabled()) {
      log.debugf("Message: %s", message.toPrettyString());
    }
    return message;
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
