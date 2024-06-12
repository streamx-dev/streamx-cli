package dev.streamx.cli.ingestion;

import dev.streamx.cli.exception.IngestionClientException;
import dev.streamx.clients.ingestion.StreamxClient;
import dev.streamx.clients.ingestion.exceptions.StreamxClientException;
import jakarta.inject.Inject;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

public abstract class BaseCommand implements Runnable {

  @ArgGroup(exclusive = false)
  protected IngestionArguments ingestionArguments;

  @Spec
  protected CommandSpec spec;

  @Inject
  protected SchemaProvider schemaProvider;

  @Inject
  protected StreamxClientProvider streamxClientProvider;

  protected abstract void performCommand(StreamxClient client) throws StreamxClientException;

  @Override
  public void run() {
    StreamxClient client = createClient();

    try {
      performCommand(client);
    } catch (StreamxClientException e) {
      if (is404Error(e)) {
        createExtendedIngestionClientException(e);
      } else {
        createIngestionClientException(e);
      }
    } finally {
      try {
        client.close();
      } catch (StreamxClientException e) {
        throw new IngestionClientException(e);
      }
    }
  }

  private StreamxClient createClient() {
    try {
      return streamxClientProvider.createStreamxClient();
    } catch (StreamxClientException e) {
      throw new IngestionClientException(e);
    }
  }

  private boolean is404Error(StreamxClientException e) {
    // TODO: extend StreamxClientException class to contain int httpStatusCode field
    //  and use it here instead of analyzing exception message
    return StringUtils.contains(e.getMessage(), "Response status: 404");
  }

  private static void createIngestionClientException(StreamxClientException e) {
    throw new IngestionClientException(e);
  }

  private void createExtendedIngestionClientException(StreamxClientException e) {
    List<String> validChannels = schemaProvider.getValidChannels();
    String exceptionMessage = e.getMessage() == null ? "" : (e.getMessage() + "; ");
    exceptionMessage += "Valid channels are: " + String.join(", ", validChannels);
    throw new IngestionClientException(new StreamxClientException(exceptionMessage, e));
  }
}
