package dev.streamx.cli.command.ingestion.unpublish;

import static dev.streamx.cli.util.Output.printf;

import com.fasterxml.jackson.databind.JsonNode;
import dev.streamx.cli.VersionProvider;
import dev.streamx.cli.command.ingestion.BaseIngestionCommand;
import dev.streamx.cli.command.ingestion.IngestionTargetArguments;
import dev.streamx.clients.ingestion.exceptions.StreamxClientException;
import dev.streamx.clients.ingestion.publisher.Publisher;
import dev.streamx.clients.ingestion.publisher.SuccessResult;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;

@Command(name = UnpublishCommand.COMMAND_NAME,
    mixinStandardHelpOptions = true,
    versionProvider = VersionProvider.class,
    description = "Send unpublication trigger"
)
public class UnpublishCommand extends BaseIngestionCommand {

  public static final String COMMAND_NAME = "unpublish";

  @ArgGroup(exclusive = false, multiplicity = "1")
  IngestionTargetArguments ingestionTargetArguments;

  @Override
  protected String getChannel() {
    return ingestionTargetArguments.getChannel();
  }

  @Override
  protected void perform(Publisher<JsonNode> publisher) throws StreamxClientException {
    SuccessResult result = publisher.unpublish(ingestionTargetArguments.getKey());

    printf("Registered unpublish trigger on '%s' with key '%s' at %d%n",
        ingestionTargetArguments.getChannel(), result.getKey(),  result.getEventTime());
  }
}
