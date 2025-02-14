package dev.streamx.cli.command.ingestion.stream.parser;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.streamx.cli.exception.PayloadException;
import dev.streamx.clients.ingestion.exceptions.StreamxClientException;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.io.InputStream;

@ApplicationScoped
public class StreamIngestionJsonParser {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  public void parse(InputStream inputStream, StreamIngestionProcessor processor)
      throws StreamxClientException {
    int index = 0;
    JsonFactory factory = new JsonFactory(OBJECT_MAPPER);
    try (JsonParser parser = factory.createParser(inputStream)) {
      JsonToken jsonToken = parser.nextToken();
      if (jsonToken != JsonToken.START_OBJECT) {
        throw new JsonParseException("No object start");
      }
      while (jsonToken == JsonToken.START_OBJECT) {
        index++;
        JsonNode node = OBJECT_MAPPER.readTree(parser);
        processor.apply(node);
        jsonToken = parser.nextToken();
      }
      if (jsonToken != null) {
        throw new JsonParseException(
            "Unexpected token '%s' at messages index %s".formatted(jsonToken, index));
      }
      parser.nextToken();
    } catch (JsonParseException e) {
      throw PayloadException.jsonParseException(e, "Cannot parse JSON");
    } catch (IOException e) {
      throw PayloadException.ioException(e);
    }
  }
}
