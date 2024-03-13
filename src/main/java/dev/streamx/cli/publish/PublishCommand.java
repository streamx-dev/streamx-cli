package dev.streamx.cli.publish;

import static dev.streamx.cli.util.ExceptionUtils.sneakyThrow;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.streamx.clients.ingestion.StreamxClient;
import dev.streamx.clients.ingestion.exceptions.StreamxClientException;
import jakarta.inject.Inject;
import org.apache.http.impl.client.CloseableHttpClient;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Visibility;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "publish", mixinStandardHelpOptions = true)
public class PublishCommand implements Runnable {

  @Parameters(index = "0", description = "Channel that message will be published to")
  String channel;

  @Parameters(index = "1", description = "Message key")
  String key;

  @Option(names = "--ingestion-url",
      description = "Address of 'rest-ingestion-service'",
      showDefaultValue = Visibility.ALWAYS,
      defaultValue = "http://localhost:8080")
  String ingestionUrl;

  @Option(names = {"-d", "--data"}, defaultValue = """
        {"content": {"bytes": "<h1>Hello World!</h1>"}}""")
  String data;

  @Inject
  SchemaProvider schemaProvider;

  @Inject
  CloseableHttpClient httpClient;

  @Override
  public void run() {
    schemaProvider.provideSchema(channel);

    try (StreamxClient client = createStreamxClient()) {
      JsonNode jsonNode = new ObjectMapper().readValue(data, JsonNode.class);
      var pagePublisher = client.newPublisher(channel, JsonNode.class);

      pagePublisher.publish(key, jsonNode);
    } catch (StreamxClientException | JsonProcessingException e) {
      throw sneakyThrow(e); // FIXME
    }
  }

  private StreamxClient createStreamxClient() throws StreamxClientException {
    return StreamxClient.builder(ingestionUrl)
        .setApacheHttpClient(httpClient)
        .build();
  }
}