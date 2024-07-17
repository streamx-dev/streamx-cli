package dev.streamx.cli.ingestion.unpublish;

import com.fasterxml.jackson.databind.JsonNode;
import dev.streamx.cli.VersionProvider;
import dev.streamx.cli.exception.IngestionClientException;
import dev.streamx.cli.exception.UnknownChannelException;
import dev.streamx.cli.ingestion.IngestionArguments;
import dev.streamx.cli.ingestion.IngestionClientConfig;
import dev.streamx.cli.ingestion.IngestionTargetArguments;
import dev.streamx.cli.ingestion.SchemaProvider;
import dev.streamx.cli.ingestion.StreamxClientProvider;
import dev.streamx.cli.util.ExceptionUtils;
import dev.streamx.clients.ingestion.StreamxClient;
import dev.streamx.clients.ingestion.exceptions.StreamxClientException;
import jakarta.inject.Inject;
import javax.net.ssl.SSLHandshakeException;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Spec;

@Command(name = UnpublishCommand.COMMAND_NAME,
    mixinStandardHelpOptions = true,
    versionProvider = VersionProvider.class,
    description = "Send unpublication trigger"
)
public class UnpublishCommand implements Runnable {
  public static final String COMMAND_NAME = "unpublish";

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

  @Inject
  IngestionClientConfig ingestionClientConfig;

  @Override
  public void run() {
    validateChannel();

    try (StreamxClient client = streamxClientProvider.createStreamxClient()) {
      var publisher = client.newPublisher(ingestionTargetArguments.getChannel(), JsonNode.class);

      Long eventTime = publisher.unpublish(ingestionTargetArguments.getKey());

      System.out.printf("Registered unpublish trigger on '%s' at %d%n",
          ingestionTargetArguments.getChannel(), eventTime);
    } catch (StreamxClientException e) {
      if (e.getCause() instanceof SSLHandshakeException) {
        throw IngestionClientException.sslException(ingestionClientConfig.url());
      }
      throw ExceptionUtils.sneakyThrow(e);
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