package dev.streamx.cli.ingestion.publish;

import com.fasterxml.jackson.databind.JsonNode;
import dev.streamx.cli.VersionProvider;
import dev.streamx.cli.ingestion.BaseIngestionCommand;
import dev.streamx.cli.ingestion.publish.payload.PayloadResolver;
import dev.streamx.cli.ingestion.publish.payload.source.FileSourceResolver;
import dev.streamx.clients.ingestion.exceptions.StreamxClientException;
import dev.streamx.clients.ingestion.publisher.Publisher;
import dev.streamx.clients.ingestion.publisher.PublisherSuccessResult;
import jakarta.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;

@Command(name = PublishCommand.COMMAND_NAME,
    mixinStandardHelpOptions = true,
    versionProvider = VersionProvider.class,
    description = "Send publication data")
public class PublishCommand extends BaseIngestionCommand {

  public static final String COMMAND_NAME = "publish";

  @ArgGroup(exclusive = false, multiplicity = "1")
  PublishTargetArguments publishTargetArguments;

  @ArgGroup(exclusive = false)
  PayloadArguments payloadArguments;

  @Inject
  PayloadResolver payloadResolver;

  @Override
  protected String getChannel() {
    return publishTargetArguments.getChannel();
  }

  @Override
  protected void perform(Publisher<JsonNode> publisher) throws StreamxClientException {
    List<PayloadArgument> mergedPayloadArgumentList = prependPayloadFile();
    JsonNode jsonNode = payloadResolver.createPayload(mergedPayloadArgumentList);
    PublisherSuccessResult result = publisher.publish(publishTargetArguments.getKey(), jsonNode);
    System.out.printf("Registered data publication on '%s' with key '%s' at %d%n",
        publishTargetArguments.getChannel(), result.getKey(), result.getEventTime());
  }

  @NotNull
  private List<PayloadArgument> prependPayloadFile() {
    var payloadFileArgument = Optional.ofNullable(publishTargetArguments)
        .map(PublishTargetArguments::getPayloadFile)
        .map(arg -> PayloadArgument.ofJsonNode(FileSourceResolver.FILE_STRATEGY_PREFIX + arg));
    var payloadArgStream = Optional.ofNullable(payloadArguments)
        .map(PayloadArguments::getPayloadArgs).stream()
        .flatMap(Collection::stream);

    return Stream.concat(payloadFileArgument.stream(), payloadArgStream).toList();
  }
}
