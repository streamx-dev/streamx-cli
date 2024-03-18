package dev.streamx.cli.publish.payload;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@ApplicationScoped
public class PayloadResolver {

  private final ObjectMapper objectMapper = new ObjectMapper();

  public JsonNode createPayload(String data) {
    try {
      String payload = getPayload(data);
      JsonNode jsonNode = objectMapper.readValue(payload, JsonNode.class);

      // TODO replace jsonpath

      return jsonNode;
    } catch (JsonProcessingException e) {
      throw PayloadException.jsonProcessingException(e);
    } catch (IOException e) {
      throw PayloadException.ioException(e);
    }
  }

  private static String getPayload(String data) throws IOException {
    if (data.startsWith("@")) {
      return readPayloadFromFile(data);
    } else {
      return data;
    }
  }

  private static String readPayloadFromFile(String data) {
    try {
      Path path = Path.of(data.substring(1));

      return Files.readString(path);
    } catch (IOException e) {
      throw PayloadException.fileReadingException(e);
    }
  }
}
