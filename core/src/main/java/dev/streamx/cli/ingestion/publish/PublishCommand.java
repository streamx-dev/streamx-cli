package dev.streamx.cli.ingestion.publish;

import com.fasterxml.jackson.databind.JsonNode;
import dev.streamx.cli.ingestion.BaseCommand;
import dev.streamx.cli.ingestion.publish.payload.PayloadResolver;
import dev.streamx.cli.ingestion.publish.payload.source.FileSourceResolver;
import dev.streamx.clients.ingestion.StreamxClient;
import dev.streamx.clients.ingestion.exceptions.StreamxClientException;
import jakarta.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;

@Command(name = "publish", mixinStandardHelpOptions = true)
public class PublishCommand extends BaseCommand {

  @ArgGroup(exclusive = false, multiplicity = "1")
  PublishTargetArguments ingestionTargetArguments;

  @ArgGroup(exclusive = false)
  PayloadArguments payloadArguments;

  @Inject
  PayloadResolver payloadResolver;

  @Override
  protected void performCommand(StreamxClient client) throws StreamxClientException {
    List<PayloadArgument> mergedPayloadArgumentList = prependPayloadFile();
    JsonNode jsonNode = payloadResolver.createPayload(mergedPayloadArgumentList);

    var publisher = client.newPublisher(ingestionTargetArguments.getChannel(), JsonNode.class);

    Long eventTime = publisher.publish(ingestionTargetArguments.getKey(), jsonNode);

    System.out.printf("Registered publish event on '%s' at %d%n",
        ingestionTargetArguments.getChannel(), eventTime);
  }

  @NotNull
  private List<PayloadArgument> prependPayloadFile() {
    var payloadFileArgument = Optional.ofNullable(ingestionTargetArguments)
        .map(PublishTargetArguments::getPayloadFile)
        .map(arg -> PayloadArgument.ofJsonNode(FileSourceResolver.FILE_STRATEGY_PREFIX + arg));
    var payloadArgStream = Optional.ofNullable(payloadArguments)
        .map(PayloadArguments::getPayloadArgs).stream()
        .flatMap(Collection::stream);

    return Stream.concat(payloadFileArgument.stream(), payloadArgStream).toList();
  }
}