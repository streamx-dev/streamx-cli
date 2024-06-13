package dev.streamx.cli.ingestion.unpublish;

import com.fasterxml.jackson.databind.JsonNode;
import dev.streamx.cli.ingestion.BaseCommand;
import dev.streamx.cli.ingestion.IngestionTargetArguments;
import dev.streamx.clients.ingestion.StreamxClient;
import dev.streamx.clients.ingestion.exceptions.StreamxClientException;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;

@Command(name = "unpublish", mixinStandardHelpOptions = true)
public class UnpublishCommand extends BaseCommand {

  @ArgGroup(exclusive = false, multiplicity = "1")
  IngestionTargetArguments ingestionTargetArguments;

  @Override
  protected void performCommand(StreamxClient client) throws StreamxClientException {
    var publisher = client.newPublisher(ingestionTargetArguments.getChannel(), JsonNode.class);

    Long eventTime = publisher.unpublish(ingestionTargetArguments.getKey());

    System.out.printf("Registered unpublish event on '%s' at %d%n",
        ingestionTargetArguments.getChannel(), eventTime);
  }
}