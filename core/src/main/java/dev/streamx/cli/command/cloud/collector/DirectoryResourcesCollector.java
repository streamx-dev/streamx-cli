package dev.streamx.cli.command.cloud.collector;

import static dev.streamx.cli.command.cloud.MetadataUtils.setManagedByAndPartOfLabels;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.streamx.cli.util.FileUtils;
import io.fabric8.kubernetes.api.model.HasMetadata;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Collects controlled resources defined in the resources directory.
 */
public class DirectoryResourcesCollector implements KubernetesResourcesCollector {

  private final ObjectMapper objectMapper;
  private final Path projectPath;
  private final List<String> resourcesDirectories;


  public DirectoryResourcesCollector(ObjectMapper objectMapper, Path projectPath,
      List<String> resourcesDirectories) {
    this.objectMapper = objectMapper;
    this.projectPath = projectPath;
    this.resourcesDirectories = resourcesDirectories;
  }

  @Override
  public List<HasMetadata> collect(String meshName) {
    List<HasMetadata> resources = new ArrayList<>();

    return resourcesDirectories.stream().flatMap(resourcesDirectory -> {
      Path dirPath = projectPath.resolve(resourcesDirectory);

      if (!Files.exists(dirPath) || !Files.isDirectory(dirPath)) {
        throw new IllegalArgumentException(
            "Kubernetes resources directory: %s does not exist".formatted(dirPath));
      }

      try (Stream<Path> files = Files.list(dirPath)) {
        files.forEach(file -> processResourceFile(file, resources));
      } catch (IOException e) {
        throw new RuntimeException("Error reading directory: " + dirPath, e);
      }

      resources.forEach(r -> setManagedByAndPartOfLabels(r, meshName));
      return resources.stream();
    }).toList();
  }

  private void processResourceFile(Path file, List<HasMetadata> resources) {
    String fileName = FileUtils.toString(file.getFileName());
    if (!fileName.endsWith(".yaml") && !fileName.endsWith(".yml")) {
      return;
    }
    try (MappingIterator<HasMetadata> iterator =
        this.objectMapper.readerFor(HasMetadata.class).readValues(file.toFile())) {

      while (iterator.hasNext()) {
        resources.add(iterator.next());
      }

    } catch (IOException e) {
      throw new RuntimeException("Failed to load resource from file: " + file, e);
    }
  }
}
