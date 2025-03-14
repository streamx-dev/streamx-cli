package dev.streamx.cli.command;

import dev.streamx.runner.StreamxRunner;
import io.quarkus.runtime.ApplicationLifecycleManager;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@ApplicationScoped
public class MeshStopper {

  private static final ScheduledExecutorService SCHEDULER =
      Executors.newSingleThreadScheduledExecutor();
  private static final AtomicBoolean scheduled =  new AtomicBoolean(false);
  @Inject
  StreamxRunner streamxRunner;

  public void scheduleStop() {
    if (!scheduled.getAndSet(true)) {
      SCHEDULER.schedule(() -> {
        streamxRunner.stopMesh();
        streamxRunner.stopBase();

        ApplicationLifecycleManager.exit();
      }, 100, TimeUnit.MILLISECONDS);
    }
  }
}
