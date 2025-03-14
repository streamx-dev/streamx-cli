package dev.streamx.cli.command.meshprocessing;

import static dev.streamx.cli.util.Output.print;

import io.quarkus.scheduler.Scheduled.ConcurrentExecution;
import io.quarkus.scheduler.Scheduler;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import org.jboss.logging.Logger;

@ApplicationScoped
public class MeshWatcher {

  public static final String MESH_WATCHER_JOB_NAME = "meshWatcher";

  @Inject
  Logger log;

  @Inject
  MeshManager meshManager;

  @Inject
  Scheduler scheduler;

  public void watchMeshChanges(Path meshPath) {
    try {
      Path meshDir = meshPath.getParent();

      WatchService watchService = FileSystems.getDefault().newWatchService();
      WatchKey watchKey = meshDir.register(watchService,
          StandardWatchEventKinds.ENTRY_CREATE,
          StandardWatchEventKinds.ENTRY_MODIFY,
          StandardWatchEventKinds.ENTRY_DELETE
      );
      watchKey.reset();

      scheduler.newJob(MESH_WATCHER_JOB_NAME)
          .setConcurrentExecution(ConcurrentExecution.SKIP)
          .setTask(task -> {
            try {
              ActionToPerform action = checkModifications(meshPath, watchService);
              if (action != null) {
                switch (action) {
                  case RELOAD -> {
                    meshManager.reload();
                  }
                  case STOP -> {
                    print("");
                    print("Mesh file deleted. Stopping...");
                    meshManager.stop();
                    print("Mesh stopped.");
                  }
                  default -> {
                    print("Unknown action: " + action + ". Skipping...");
                  }
                }
              }
            } catch (Exception e) {
              log.error(e);
            }
          })
          .setInterval("500ms")
          .schedule();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static ActionToPerform checkModifications(Path meshPath, WatchService watchService) {
    ActionToPerform lastAction = null;
    WatchKey polledKey = watchService.poll();
    if (polledKey != null) {
      for (var event : polledKey.pollEvents()) {
        if (pathsMatches(meshPath, event)) {
          if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
            lastAction = ActionToPerform.RELOAD;
          } else if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
            lastAction = ActionToPerform.RELOAD;
          } else if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
            lastAction = ActionToPerform.STOP;
          }
        }
      }
      polledKey.reset();
    }
    return lastAction;
  }

  private enum ActionToPerform {
    RELOAD,
    STOP
  }

  private static boolean pathsMatches(Path meshPath, WatchEvent<?> event) {
    Path modifiedFile = (Path) event.context();
    Path normalizedModifiedPath = meshPath.getParent().resolve(modifiedFile)
        .toAbsolutePath()
        .normalize();

    Path normalizedMeshPath = meshPath.toAbsolutePath().normalize();

    return normalizedModifiedPath.equals(normalizedMeshPath);
  }
}
