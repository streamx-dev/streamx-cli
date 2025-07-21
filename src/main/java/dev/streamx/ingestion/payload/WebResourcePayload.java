package dev.streamx.ingestion.payload;

import dev.streamx.exception.GitHubActionException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;

public class WebResourcePayload {

  private static final Logger log = Logger.getLogger(WebResourcePayload.class);

  private final String workspace;

  private final String filePath;

  private final String absolutePath;

  public WebResourcePayload(String workspace, String filePath) {
    this.workspace = workspace;
    this.filePath = filePath;
    this.absolutePath = resolveAbsolutPath();
  }

  private String resolveAbsolutPath() {
    return StringUtils.startsWith(filePath, File.separator) ?
        workspace + filePath :
        workspace + File.separator + filePath;
  }

  public RawPayload resolve() throws GitHubActionException {
    byte[] bytes = readFile(absolutePath);
    log.debug(String.format("Read file: %s, bytes length: %d", absolutePath, bytes.length));
    return new RawPayload(bytes);
  }

  public String getFilePath() {
    return filePath;
  }

  private byte[] readFile(String data) throws GitHubActionException {
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
