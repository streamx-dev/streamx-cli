package dev.streamx.cli.unpublish;

import com.fasterxml.jackson.databind.JsonNode;
import dev.streamx.cli.exception.UnknownChannelException;
import dev.streamx.cli.ingestion.IngestionClientContext;
import dev.streamx.cli.ingestion.IngestionClientException;
import dev.streamx.cli.ingestion.SchemaProvider;
import dev.streamx.cli.ingestion.StreamxClientProvider;
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

@Command(name = "unpublish", mixinStandardHelpOptions = true)
public class UnpublishCommand implements Runnable {

  @Parameters(index = "0", description = "Channel that message will be unpublished to")
  String channel;

  @Parameters(index = "1", description = "Message key")
  String key;

  @Option(names = "--ingestion-url",
      description = "Address of 'rest-ingestion-service'",
      showDefaultValue = Visibility.ALWAYS,
      defaultValue = "http://localhost:8080")
  void propagateIngestionUrl(String ingestionUrl) {
    ingestionClientContext.setIngestionUrl(ingestionUrl);
  }

  @Spec
  CommandSpec spec;

  @Inject
  SchemaProvider schemaProvider;

  @Inject
  StreamxClientProvider streamxClientProvider;

  @Inject
  IngestionClientContext ingestionClientContext;

  @Override
  public void run() {
    validateChannel();

    try (StreamxClient client = streamxClientProvider.createStreamxClient()) {
      var publisher = client.newPublisher(channel, JsonNode.class);

      publisher.unpublish(key);
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