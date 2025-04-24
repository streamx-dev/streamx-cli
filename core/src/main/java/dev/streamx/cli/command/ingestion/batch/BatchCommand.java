package dev.streamx.cli.command.ingestion.batch;

import static dev.streamx.cli.util.Output.printf;

import com.fasterxml.jackson.databind.JsonNode;
import dev.streamx.cli.VersionProvider;
import dev.streamx.cli.command.ingestion.BaseIngestionCommand;
import dev.streamx.cli.command.ingestion.IngestionMessageJsonFactory;
import dev.streamx.cli.command.ingestion.batch.BatchIngestionArguments.ActionType;
import dev.streamx.cli.command.ingestion.batch.exception.EventSourceDescriptorException;
import dev.streamx.cli.command.ingestion.batch.exception.FileIngestionException;
import dev.streamx.cli.command.ingestion.batch.resolver.BatchPayloadResolver;
import dev.streamx.cli.command.ingestion.batch.resolver.substitutor.Substitutor;
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
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParameterException;

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
  BatchPayloadResolver payloadResolver;

  @Inject
  Substitutor substitutor;

  @Inject
  IngestionMessageJsonFactory ingestionMessageJsonFactory;

  private State state;

  @Override
  protected String getChannel() {
    return state.channel();
  }

  protected void doRun(StreamxClient client) throws StreamxClientException {
    Path startDir = Paths.get(batchIngestionArguments.getSourceDirectory());

    try {
      if (!Files.exists(startDir)) {
        throw new RuntimeException("Directory '" + startDir + "' does not exists.");
      }
      if (!Files.isDirectory(startDir)) {
        throw new RuntimeException("Specified path '" + startDir + "' must be a directory.");
      }

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
          throw new FileIngestionException(file, ex);
        }
      }));
    } catch (FileIngestionException e) {
      throw new RuntimeException(
          ExceptionUtils.appendLogSuggestion(
              "Error performing batch publication while processing '" + e.getPath() + "' file.\n"
              + "\n"
              + "Details:\n"
              + e.getCause().getMessage()), e);
    } catch (EventSourceDescriptorException e) {
      throw new RuntimeException(
          ExceptionUtils.appendLogSuggestion(
              "Invalid descriptor: '" + e.getPath() + "'.\n"
              + "\n"
              + "Details:\n"
              + e.getCause().getMessage()), e);
    } catch (NoSuchFileException e) {
      throw new RuntimeException("File '" + e.getFile() + "' does not exists.");
    } catch (IOException e) {
      throw new RuntimeException(
          ExceptionUtils.appendLogSuggestion(
              "Error performing batch publication using '" + startDir + "' directory.\n"
                  + "\n"
                  + "Details:\n"
                  + e.getMessage()), e);
    }
  }

  @Override
  protected void perform(Publisher<JsonNode> publisher) throws StreamxClientException {
    String schemaType = schemaTypesCache.computeIfAbsent(getChannel(),
        c -> getPayloadPropertyName());
    ActionType action = state.action();
    SuccessResult result = publisher.send(ingestionMessageJsonFactory.from(
        state.key(),
        action.toString(),
        state.message(),
        state.properties(),
        schemaType
    ));

    printf("Sent data %s message using batch to '%s' with key '%s' at %d%n",
        action, state.channel(), state.key(),
        result.getEventTime());

  }

  private void updateCommandState(Path file, EventSourceDescriptor eventSource) {

    String relativePath = calculateRelativePath(file, eventSource);
    Map<String, String> variables = substitutor.createSubstitutionVariables(
        FileUtils.toString(file), eventSource.getChannel(), relativePath);

    String key = substitutor.substitute(variables, eventSource.getKey());
    JsonNode message = executeHandlingException(
        () -> payloadResolver.createPayload(eventSource, variables),
        () -> "Could not resolve payload for file '" + file + "'"
    );

    Map<String, String> properties = createProperties(eventSource, variables);

    this.state = new State(
        eventSource.getChannel(), key, properties, message, batchIngestionArguments.getAction()
    );
  }

  private @NotNull Map<String, String> createProperties(EventSourceDescriptor eventSource,
      Map<String, String> variables) {
    final Map<String, String> properties = new HashMap<>();
    if (eventSource.getProperties() != null) {
      for (Entry<String, String> entry : eventSource.getProperties().entrySet()) {
        properties.put(entry.getKey(), substitutor.substitute(variables, entry.getValue()));
      }
    }
    return properties;
  }

  private <T> T executeHandlingException(Supplier<T> function,
      Supplier<String> messageSupplier) {
    try {
      return function.get();
    } catch (RuntimeException e) {
      throw new ParameterException(spec.commandLine(),
          messageSupplier.get() + "\n"
          + "\n"
          + "Details:\n" + e.getMessage());
    }
  }

  @NotNull
  private String calculateRelativePath(Path file, EventSourceDescriptor eventSource) {
    String relativePath;
    if (eventSource.getRelativePathLevel() == null) {
      relativePath = FileUtils.toString(
          Path.of(batchIngestionArguments.getSourceDirectory()).relativize(file));
    } else {
      relativePath = FileUtils.toString(FileUtils.getNthParent(eventSource.getSource(),
          eventSource.getRelativePathLevel()).relativize(file));
    }
    return relativePath;
  }

  private record State(String channel, String key, Map<String, String> properties, JsonNode message,
                       ActionType action) {

  }
}
