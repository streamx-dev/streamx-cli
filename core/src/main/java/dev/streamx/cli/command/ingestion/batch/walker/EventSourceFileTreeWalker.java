package dev.streamx.cli.command.ingestion.batch.walker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import dev.streamx.cli.command.ingestion.batch.EventSourceDescriptor;
import io.quarkus.logging.Log;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Stack;

public class EventSourceFileTreeWalker extends SimpleFileVisitor<Path> {

  private final Stack<EventSourceDescriptor> eventSourceStack = new Stack<>();
  private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

  private final EventSourceProcessor processor;

  public EventSourceFileTreeWalker(EventSourceProcessor processor) {
    this.processor = processor;
  }

  @Override
  public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
    Path configFile = dir.resolve(EventSourceDescriptor.FILENAME);
    if (Files.exists(configFile) && Files.isRegularFile(configFile)) {
      EventSourceDescriptor descriptor = yamlMapper.readValue(configFile.toFile(),
          EventSourceDescriptor.class);
      descriptor.setSource(configFile);

      eventSourceStack.push(descriptor);
      Log.debugf("Found event source in %s: %s", dir, descriptor);
    }

    // Process all files in this directory with the current active event source (if any)
    processDirectory(dir, eventSourceStack.isEmpty() ? null : eventSourceStack.peek());
    return FileVisitResult.CONTINUE;
  }

  @Override
  public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
    Path configFile = dir.resolve(EventSourceDescriptor.FILENAME);
    if (Files.exists(configFile) && Files.isRegularFile(configFile)) {
      EventSourceDescriptor popped = eventSourceStack.pop();
      Log.debugf("Leaving %s. Reverting event source: %s", dir, popped);
    }
    return FileVisitResult.CONTINUE;
  }

  /**
   * Processes each regular file in the given directory, if there is an active event source.
   *
   * @param dir               the directory to process.
   * @param currentDescriptor the currently active event source configuration, or null if none.
   */
  private void processDirectory(Path dir, EventSourceDescriptor currentDescriptor)
      throws IOException {
    Log.debugf("Processing directory: %s", dir);
    if (currentDescriptor != null) {
      Log.debugf("Active event source: %s", currentDescriptor);

      // Iterate through all entries in the directory.
      try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
        for (Path entry : stream) {
          if (Files.isRegularFile(entry)
              // Skip .eventsource.yaml files
              && !EventSourceDescriptor.FILENAME.equals(entry.getFileName().toString())) {
            processor.apply(entry, currentDescriptor);
          }
        }
      }

    } else {
      Log.debugf("No active event source.");
    }
  }
}
