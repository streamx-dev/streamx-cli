package dev.streamx.cli.ingestion;

import com.fasterxml.jackson.databind.JsonNode;
import dev.streamx.cli.exception.IngestionClientException;
import dev.streamx.cli.exception.UnableToConnectIngestionServiceException;
import dev.streamx.cli.util.ExceptionUtils;
import dev.streamx.clients.ingestion.StreamxClient;
import dev.streamx.clients.ingestion.exceptions.StreamxClientConnectionException;
import dev.streamx.clients.ingestion.exceptions.StreamxClientException;
import dev.streamx.clients.ingestion.exceptions.StreamxClientUnsupportedChannelException;
import dev.streamx.clients.ingestion.publisher.Publisher;
import jakarta.inject.Inject;
import javax.net.ssl.SSLHandshakeException;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Spec;

public abstract class BaseIngestionCommand implements Runnable {

  @ArgGroup(exclusive = false)
  IngestionArguments ingestionArguments;

  @Spec
  CommandSpec spec;

  @Inject
  StreamxClientProvider streamxClientProvider;

  @Inject
  IngestionClientConfig ingestionClientConfig;

  protected abstract String getChannel();

  protected abstract void perform(Publisher<JsonNode> publisher) throws StreamxClientException;

  @Override
  public final void run() {
    try (StreamxClient client = streamxClientProvider.createStreamxClient(ingestionClientConfig)) {
      Publisher<JsonNode> publisher = client.newPublisher(getChannel(), JsonNode.class);
      perform(publisher);
    } catch (StreamxClientUnsupportedChannelException e) {
      throw new ParameterException(spec.commandLine(), e.getMessage());
    } catch (StreamxClientConnectionException e) {
      throw new UnableToConnectIngestionServiceException(ingestionClientConfig.url(), e);
    } catch (StreamxClientException e) {
      if (e.getCause() instanceof SSLHandshakeException) {
        throw IngestionClientException.sslException(ingestionClientConfig.url());
      }
      throw ExceptionUtils.sneakyThrow(e);
    }
  }
}