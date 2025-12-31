package dev.streamx.cli.command.ingestion.batch;

import static dev.streamx.cli.util.Output.printf;

import com.fasterxml.jackson.databind.JsonNode;
import com.streamx.clients.ingestion.exceptions.StreamxClientException;
import com.streamx.clients.ingestion.publisher.Publisher;
import dev.streamx.cli.VersionProvider;
import dev.streamx.cli.command.ingestion.BaseIngestionCommand;
import dev.streamx.cli.command.ingestion.CloudEventBuilder;
import dev.streamx.cli.command.ingestion.batch.exception.EventSourceDescriptorException;
import dev.streamx.cli.command.ingestion.batch.exception.FileIngestionException;
import dev.streamx.cli.command.ingestion.batch.resolver.BatchPayloadResolver;
import dev.streamx.cli.command.ingestion.batch.resolver.substitutor.Substitutor;
import dev.streamx.cli.command.ingestion.batch.walker.EventSourceFileTreeWalker;
import dev.streamx.cli.util.ExceptionUtils;
import dev.streamx.cli.util.FileUtils;
import io.cloudevents.CloudEvent;
import jakarta.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParameterException;

@Command(name = BatchCommand.COMMAND_NAME,
    mixinStandardHelpOptions = true,
    versionProvider = VersionProvider.class,
    description = "Send batch events from directory"
)
public class BatchCommand extends BaseIngestionCommand {

  // TODO "_v2" is a temporary postfix for now
  public static final String COMMAND_NAME = "batch_v2";

  @ArgGroup(exclusive = false, multiplicity = "1")
  BatchIngestionArguments batchIngestionArguments;

  @Inject
  BatchPayloadResolver payloadResolver;

  @Inject
  Substitutor substitutor;

  @Override
  protected void perform(Publisher publisher) {
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
          State state = updateCommandState(file, eventSource);
          send(state, publisher);
        } catch (StreamxClientException ex) {
          throw new FileIngestionException(file, ex);
        }
      }));
    } catch (FileIngestionException e) {
      throw new RuntimeException(
          ExceptionUtils.appendLogSuggestion(
              "Error performing batch publication while processing '"
              + FileUtils.toString(e.getPath()) + "' file.\n"
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

  private void send(State state, Publisher publisher) throws StreamxClientException {
    CloudEvent inputEvent = CloudEventBuilder.build(
        state.eventSubject,
        state.eventType,
        state.eventSource,
        state.payload // TODO rename payload to data everywhere
    );
    CloudEvent responseEvent = publisher.send(inputEvent);

    printf("Sent %s event using batch with key '%s' at %s%n",
        state.eventType, state.eventSubject(),
        responseEvent.getTime());
  }

  private State updateCommandState(Path file, EventSourceDescriptor eventSource) {
    String relativePath = calculateRelativePath(file, eventSource);
    Map<String, String> variables = substitutor.createSubstitutionVariables(
        FileUtils.toString(file), eventSource.getEventType(), eventSource.getEventSource(),
        relativePath);

    String key = substitutor.substitute(variables, eventSource.getKey());
    JsonNode payload = executeHandlingException(
        () -> payloadResolver.createPayload(eventSource, variables),
        () -> "Could not resolve payload for file '" + FileUtils.toString(file) + "'"
    );

    return new State(
        eventSource.getEventType(), eventSource.getEventSource(), key, payload
    );
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

  private record State(String eventType, String eventSource, String eventSubject,
                       JsonNode payload) {

  }
}
