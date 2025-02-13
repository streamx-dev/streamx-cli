package dev.streamx.cli.command.ingestion.publish;

import static dev.streamx.cli.util.Output.printf;

import com.fasterxml.jackson.databind.JsonNode;
import dev.streamx.cli.VersionProvider;
import dev.streamx.cli.command.ingestion.BaseIngestionCommand;
import dev.streamx.cli.command.ingestion.IngestionMessageJsonFactory;
import dev.streamx.cli.command.ingestion.publish.payload.PayloadResolver;
import dev.streamx.cli.util.FileSourceUtils;
import dev.streamx.clients.ingestion.exceptions.StreamxClientException;
import dev.streamx.clients.ingestion.publisher.Publisher;
import dev.streamx.clients.ingestion.publisher.SuccessResult;
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
  PublishArguments publishArguments;

  @ArgGroup(exclusive = false)
  PayloadArguments payloadArguments;

  @Inject
  PayloadResolver payloadResolver;

  @Inject
  IngestionMessageJsonFactory ingestionMessageJsonFactory;

  @Override
  protected String getChannel() {
    return publishArguments.getChannel();
  }

  @Override
  protected void perform(Publisher<JsonNode> publisher) throws StreamxClientException {
    JsonNode message = prepareIngestionMessage();

    SuccessResult result = publisher.send(message);
    printf("Sent data publication message to '%s' with key '%s' at %d%n",
        publishArguments.getChannel(), result.getKey(), result.getEventTime());
  }

  private JsonNode prepareIngestionMessage() {
    List<PayloadArgument> mergedPayloadArgumentList = prependPayloadFile();
    JsonNode payload = payloadResolver.createPayload(mergedPayloadArgumentList);
    String payloadPropertyName = getPayloadPropertyName();
    return ingestionMessageJsonFactory.from(
        publishArguments.getKey(),
        COMMAND_NAME,
        payload,
        payloadPropertyName
    );
  }

  @NotNull
  private List<PayloadArgument> prependPayloadFile() {
    var payloadFileArgument = Optional.ofNullable(publishArguments)
        .map(PublishArguments::getPayloadFile)
        .map(arg -> PayloadArgument.ofJsonNode(FileSourceUtils.FILE_STRATEGY_PREFIX + arg));
    var payloadArgStream = Optional.ofNullable(payloadArguments)
        .map(PayloadArguments::getPayloadArgs).stream()
        .flatMap(Collection::stream);

    return Stream.concat(payloadFileArgument.stream(), payloadArgStream).toList();
  }
}
