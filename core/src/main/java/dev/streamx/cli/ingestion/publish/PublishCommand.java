package dev.streamx.cli.ingestion.publish;

import com.fasterxml.jackson.databind.JsonNode;
import dev.streamx.cli.VersionProvider;
import dev.streamx.cli.exception.IngestionClientException;
import dev.streamx.cli.exception.UnknownChannelException;
import dev.streamx.cli.ingestion.IngestionArguments;
import dev.streamx.cli.ingestion.IngestionClientConfig;
import dev.streamx.cli.ingestion.SchemaProvider;
import dev.streamx.cli.ingestion.StreamxClientProvider;
import dev.streamx.cli.ingestion.publish.payload.PayloadResolver;
import dev.streamx.cli.ingestion.publish.payload.source.FileSourceResolver;
import dev.streamx.cli.util.ExceptionUtils;
import dev.streamx.clients.ingestion.StreamxClient;
import dev.streamx.clients.ingestion.exceptions.StreamxClientException;
import jakarta.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import javax.net.ssl.SSLHandshakeException;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Spec;

@Command(name = "publish",
    mixinStandardHelpOptions = true,
    versionProvider = VersionProvider.class,
    description = "Send publication data")
public class PublishCommand implements Runnable {

  @ArgGroup(exclusive = false, multiplicity = "1")
  PublishTargetArguments ingestionTargetArguments;

  @ArgGroup(exclusive = false)
  IngestionArguments ingestionArguments;

  @ArgGroup(exclusive = false)
  PayloadArguments payloadArguments;

  @Spec
  CommandSpec spec;

  @Inject
  SchemaProvider schemaProvider;

  @Inject
  StreamxClientProvider streamxClientProvider;

  @Inject
  PayloadResolver payloadResolver;

  @Inject
  IngestionClientConfig ingestionClientConfig;

  @Override
  public void run() {

    List<PayloadArgument> mergedPayloadArgumentList = prependPayloadFile();
    JsonNode jsonNode = payloadResolver.createPayload(mergedPayloadArgumentList);

    validateChannel();
    try (StreamxClient client = streamxClientProvider.createStreamxClient()) {
      var publisher = client.newPublisher(ingestionTargetArguments.getChannel(), JsonNode.class);

      Long eventTime = publisher.publish(ingestionTargetArguments.getKey(), jsonNode);

      System.out.printf("Registered data publication on '%s' at %d%n",
          ingestionTargetArguments.getChannel(), eventTime);
    } catch (StreamxClientException e) {
      if (e.getCause() instanceof SSLHandshakeException) {
        throw IngestionClientException.sslException(ingestionClientConfig.url());
      }
      throw ExceptionUtils.sneakyThrow(e);
    }
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