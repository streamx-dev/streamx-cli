package dev.streamx.cli.publish.payload;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

@ApplicationScoped
public class PayloadResolver {

  private static final ObjectMapper objectMapper = new ObjectMapper();
  static {
    objectMapper.enable(Feature.ALLOW_SINGLE_QUOTES);
  }

  public JsonNode createPayload(String data) {
    String payload = null;
    try {
      payload = getPayload(data);
      JsonNode jsonNode = objectMapper.readValue(payload, JsonNode.class);

      // TODO replace jsonpath

      return jsonNode;
    } catch (JsonParseException e) {
      throw PayloadException.jsonParseException(e, payload);
    } catch (JsonProcessingException e) {
      throw PayloadException.genericJsonProcessingException(e, payload);
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
    Path path = Path.of(data.substring(1));
    try {
      return Files.readString(path);
    } catch (NoSuchFileException e) {
      throw PayloadException.noSuchFileException(e, path);
    } catch (IOException e) {
      throw PayloadException.fileReadingException(e, path);
    }
  }
}
