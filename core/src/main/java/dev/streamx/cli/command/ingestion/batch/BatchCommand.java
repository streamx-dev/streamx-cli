package dev.streamx.cli.command.ingestion.batch;

import static dev.streamx.cli.util.Output.printf;

import com.fasterxml.jackson.databind.JsonNode;
import dev.streamx.cli.VersionProvider;
import dev.streamx.cli.command.ingestion.BaseIngestionCommand;
import dev.streamx.cli.command.ingestion.IngestionMessageJsonFactory;
import dev.streamx.cli.command.ingestion.batch.BatchIngestionArguments.ActionType;
import dev.streamx.cli.command.ingestion.batch.payload.BatchFilePayloadResolver;
import dev.streamx.cli.command.ingestion.batch.walker.EventSourceFileTreeWalker;
import dev.streamx.cli.util.ExceptionUtils;
import dev.streamx.cli.util.FileUtils;
import dev.streamx.clients.ingestion.StreamxClient;
import dev.streamx.clients.ingestion.exceptions.StreamxClientException;
import dev.streamx.clients.ingestion.publisher.Publisher;
import dev.streamx.clients.ingestion.publisher.SuccessResult;
import jakarta.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;

@Command(name = BatchCommand.COMMAND_NAME,
    mixinStandardHelpOptions = true,
    versionProvider = VersionProvider.class,
    description = "Send batch messages from directory"
)
public class BatchCommand extends BaseIngestionCommand {

  public static final String COMMAND_NAME = "batch";
  private final Map<String, Publisher<JsonNode>> publishersCache = new HashMap<>();
  private final Map<String, String> schemaTypesCache = new HashMap<>();
  @ArgGroup(exclusive = false, multiplicity = "1")
  BatchIngestionArguments batchIngestionArguments;
  @Inject
  BatchFilePayloadResolver payloadResolver;
  @Inject
  IngestionMessageJsonFactory ingestionMessageJsonFactory;
  private State state;

  @Override
  protected String getChannel() {
    return state.channel();
  }

  protected void doRun(StreamxClient client) throws StreamxClientException {
    Path startDir = Paths.get(batchIngestionArguments.sourceDirectory);

    try {
      Files.walkFileTree(startDir, new EventSourceFileTreeWalker((file, eventSource) -> {
        try {
          updateCommandState(file, eventSource);
          Publisher<JsonNode> publisher = publishersCache.get(getChannel());
          if (publisher == null) {
            publisher = client.newPublisher(getChannel(), JsonNode.class);
            publishersCache.put(getChannel(), publisher);
          }
          perform(publisher);
        } catch (StreamxClientException ex) {
          // Wrap into IOException to match method signatures
          throw new IOException(ex);
        }
      }));
    } catch (IOException e) {
      if (e.getCause() instanceof StreamxClientException) {
        // Rethrow original Exception to leverage generic publication error handling from super
        throw (StreamxClientException) e.getCause();
      } else {
        throw new RuntimeException(
            ExceptionUtils.appendLogSuggestion(
                "Error performing batch publication using '" + startDir + "' directory.\n"
                    + "\n"
                    + "Details:\n"
                    + e.getMessage()), e);
      }
    }
  }

  @Override
  protected void perform(Publisher<JsonNode> publisher) throws StreamxClientException {
    // DONE: Ignore .eventsource.yaml
    // DONE: Add calculation of relativePath based on defined root Level
    // DONE: Add Ignoring on pattern
    // TODO: Add support for publishing data from JSONs
    // TODO: Add support for publishing data from streams

    String schemaType = schemaTypesCache.computeIfAbsent(getChannel(),
        c -> getPayloadPropertyName());
    SuccessResult result = publisher.send(ingestionMessageJsonFactory.from(
        state.key(),
        state.action().toString(),
        state.message(),
        schemaType
    ));

    printf("Sent data %s message using batch to '%s' with key '%s' at %d%n",
        state.action(), state.channel(), state.key(),
        result.getEventTime());

  }

  private void updateCommandState(Path file, EventSourceDescriptor eventSource) {

    String relativePath = calculateRelativePath(file, eventSource);

    Map<String, String> variables = payloadResolver.createSubstitutionVariables(file, eventSource,
        relativePath);
    String key = payloadResolver.substitute(variables, eventSource.getKey());

    JsonNode message = payloadResolver.createPayload(eventSource, variables);

    this.state = new State(
        eventSource.getChannel(), key, message, batchIngestionArguments.getAction()
    );
  }

  @NotNull
  private String calculateRelativePath(Path file, EventSourceDescriptor eventSource) {
    String relativePath;
    if (eventSource.getRelativePathLevel() == null) {
      relativePath = Path.of(batchIngestionArguments.getSourceDirectory()).relativize(file)
          .toString();
    } else {
      relativePath = FileUtils.getNthParent(eventSource.getSource(),
              eventSource.getRelativePathLevel()).relativize(file).toString();
    }
    return relativePath;
  }

  private record State(String channel, String key, JsonNode message, ActionType action) {

  }


}