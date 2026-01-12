package dev.streamx.cli.command.ingestion.stream;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;

class StreamProperties {

  private static final Logger logger = Logger.getLogger(StreamProperties.class);
  private static final String PROPERTIES_FILE_NAME = ".stream.properties";
  private static final String JSON_FIELDS_AS_BASE64 = "json.fields.as-base64";

  private StreamProperties() {
    // no instances
  }

  public static List<String> getJsonFieldsToEncodeToBase64(Path streamFile) {
    Path propertiesFile = streamFile.resolveSibling(PROPERTIES_FILE_NAME);
    if (Files.exists(propertiesFile)) {
      try (InputStream fileStream = Files.newInputStream(propertiesFile)) {
        Properties properties = new Properties();
        properties.load(fileStream);
        String fields = properties.getProperty(JSON_FIELDS_AS_BASE64);
        if (StringUtils.isNotBlank(fields)) {
          return Arrays.stream(fields.split(","))
              .map(String::trim)
              .filter(StringUtils::isNotEmpty)
              .toList();
        }
      } catch (IOException ex) {
        logger.warn("Error reading properties from " + propertiesFile, ex);
      }
    }
    return Collections.emptyList();
  }

}