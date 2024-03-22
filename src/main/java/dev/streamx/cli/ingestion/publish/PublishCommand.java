package dev.streamx.cli.ingestion.publish;

import com.fasterxml.jackson.databind.JsonNode;
import dev.streamx.cli.exception.UnknownChannelException;
import dev.streamx.cli.ingestion.IngestionArguments;
import dev.streamx.cli.ingestion.IngestionClientException;
import dev.streamx.cli.ingestion.IngestionTargetArguments;
import dev.streamx.cli.ingestion.SchemaProvider;
import dev.streamx.cli.ingestion.StreamxClientProvider;
import dev.streamx.cli.ingestion.publish.payload.PayloadResolver;
import dev.streamx.clients.ingestion.StreamxClient;
import dev.streamx.clients.ingestion.exceptions.StreamxClientException;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Spec;

@Command(name = "publish", mixinStandardHelpOptions = true)
public class PublishCommand implements Runnable {

  @ArgGroup(exclusive = false)
  IngestionTargetArguments ingestionTargetArguments;

  @ArgGroup(exclusive = false)
  IngestionArguments ingestionArguments;

  @Option(names = {"-d", "--data"},
      description = "Published payload",
      required = true)
  String data;

  @ArgGroup(exclusive = false, multiplicity = "0..*")
  List<ValueArguments> values = new ArrayList<>();

  @Spec
  CommandSpec spec;

  @Inject
  SchemaProvider schemaProvider;

  @Inject
  StreamxClientProvider streamxClientProvider;

  @Inject
  PayloadResolver payloadResolver;

  @Override
  public void run() {
    validateChannel();

    JsonNode jsonNode = payloadResolver.createPayload(data, values);

    try (StreamxClient client = streamxClientProvider.createStreamxClient()) {
      var publisher = client.newPublisher(ingestionTargetArguments.getChannel(), JsonNode.class);

      publisher.publish(ingestionTargetArguments.getKey(), jsonNode);
    } catch (StreamxClientException e) {
      throw new IngestionClientException(e);
    }
  }

  private void validateChannel() {
    try {
      schemaProvider.validateChannel(ingestionTargetArguments.getChannel());
    } catch (UnknownChannelException exception) {
      throw new ParameterException(spec.commandLine(),
          "Channel '" + exception.getChannel() + "' not found. "
          + "Available channels: " + exception.getAvailableChannels());
    }
  }
}