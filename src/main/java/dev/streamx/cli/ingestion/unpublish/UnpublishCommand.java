package dev.streamx.cli.ingestion.unpublish;

import com.fasterxml.jackson.databind.JsonNode;
import dev.streamx.cli.exception.UnknownChannelException;
import dev.streamx.cli.ingestion.IngestionArguments;
import dev.streamx.cli.ingestion.IngestionClientContext;
import dev.streamx.cli.ingestion.IngestionClientException;
import dev.streamx.cli.ingestion.IngestionTargetArguments;
import dev.streamx.cli.ingestion.SchemaProvider;
import dev.streamx.cli.ingestion.StreamxClientProvider;
import dev.streamx.clients.ingestion.StreamxClient;
import dev.streamx.clients.ingestion.exceptions.StreamxClientException;
import jakarta.inject.Inject;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Visibility;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

@Command(name = "unpublish", mixinStandardHelpOptions = true)
public class UnpublishCommand implements Runnable {

  @ArgGroup(exclusive = false, multiplicity = "1")
  IngestionTargetArguments ingestionTargetArguments;

  @ArgGroup(exclusive = false)
  IngestionArguments ingestionArguments;

  @Spec
  CommandSpec spec;

  @Inject
  SchemaProvider schemaProvider;

  @Inject
  StreamxClientProvider streamxClientProvider;

  @Override
  public void run() {
    validateChannel();

    try (StreamxClient client = streamxClientProvider.createStreamxClient()) {
      var publisher = client.newPublisher(ingestionTargetArguments.getChannel(), JsonNode.class);

      publisher.unpublish(ingestionTargetArguments.getKey());
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