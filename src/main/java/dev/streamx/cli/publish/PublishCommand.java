package dev.streamx.cli.publish;

import com.fasterxml.jackson.databind.JsonNode;
import dev.streamx.cli.ingestionclient.IngestionClientContext;
import dev.streamx.cli.ingestionclient.IngestionClientException;
import dev.streamx.cli.ingestionclient.StreamxClientProvider;
import dev.streamx.cli.publish.payload.PayloadResolver;
import dev.streamx.clients.ingestion.StreamxClient;
import dev.streamx.clients.ingestion.exceptions.StreamxClientException;
import jakarta.inject.Inject;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Visibility;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

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
  void propagateIngestionUrl(String ingestionUrl) {
    ingestionClientContext.setIngestionUrl(ingestionUrl);
  };

  @Option(names = {"-d", "--data"},
      description = "Published payload",
      required = true)
  String data;

  @Spec
  CommandSpec spec;

  @Inject
  SchemaProvider schemaProvider;

  @Inject
  StreamxClientProvider streamxClientProvider;

  @Inject
  IngestionClientContext ingestionClientContext;

  @Inject
  PayloadResolver payloadResolver;

  @Override
  public void run() {
    validateChannel();

    JsonNode jsonNode = payloadResolver.createPayload(data);

    try (StreamxClient client = streamxClientProvider.createStreamxClient()) {
      var jsonPublisher = client.newPublisher(channel, JsonNode.class);

      jsonPublisher.publish(key, jsonNode);
    } catch (StreamxClientException e) {
      throw new IngestionClientException(e);
    }
  }

  private void validateChannel() {
    try {
      schemaProvider.validateChannel(channel);
    } catch (UnknownChannelException exception) {
      throw new ParameterException(spec.commandLine(),
          "Channel '" + exception.getChannel() + "' not found. "
          + "Available channels: " + exception.getAvailableChannels());

    }
  }
}