package dev.streamx.cli.ingestion.publish;

import com.fasterxml.jackson.databind.JsonNode;
import dev.streamx.cli.exception.IngestionClientException;
import dev.streamx.cli.exception.UnknownChannelException;
import dev.streamx.cli.ingestion.IngestionArguments;
import dev.streamx.cli.ingestion.SchemaProvider;
import dev.streamx.cli.ingestion.StreamxClientProvider;
import dev.streamx.cli.ingestion.publish.DataArguments.DataType;
import dev.streamx.cli.ingestion.publish.payload.PayloadResolver;
import dev.streamx.cli.ingestion.publish.payload.source.FileSourceResolver;
import dev.streamx.clients.ingestion.StreamxClient;
import dev.streamx.clients.ingestion.exceptions.StreamxClientException;
import jakarta.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Spec;

@Command(name = "publish", mixinStandardHelpOptions = true)
public class PublishCommand implements Runnable {

  @ArgGroup(exclusive = false, multiplicity = "1")
  PublishTargetArguments ingestionTargetArguments;

  @ArgGroup(exclusive = false)
  IngestionArguments ingestionArguments;

  @ArgGroup(exclusive = false, heading = "\n@|bold Payload arguments:|@\n")
  PayloadArguments payloadArguments;

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

    var payloadFileArgument = Optional.ofNullable(ingestionTargetArguments)
        .map(PublishTargetArguments::getPayloadFile)
        .map(arg -> {
          DataArguments payloadFileAsDataArgument = new DataArguments();
          payloadFileAsDataArgument.value =
              FileSourceResolver.FILE_STRATEGY_PREFIX + arg;
          payloadFileAsDataArgument.dataType = new DataType();
          payloadFileAsDataArgument.dataType.json = true;

          return payloadFileAsDataArgument;
        });
    var dataArgumentsStream = Optional.ofNullable(payloadArguments)
        .map(PayloadArguments::getDataArgs).stream()
        .flatMap(Collection::stream);

    List<DataArguments> mergedDataArgumentsList = Stream.concat(payloadFileArgument.stream(),
            dataArgumentsStream)
        .collect(Collectors.toList());

    JsonNode jsonNode = payloadResolver.createPayload(mergedDataArgumentsList);

    try (StreamxClient client = streamxClientProvider.createStreamxClient()) {
      var publisher = client.newPublisher(ingestionTargetArguments.getChannel(), JsonNode.class);

      Long eventTime = publisher.publish(ingestionTargetArguments.getKey(), jsonNode);

      System.out.printf("Registered publish event on '%s' at %d%n",
          ingestionTargetArguments.getChannel(), eventTime);
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