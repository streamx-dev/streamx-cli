package dev.streamx.cli.command.meshprocessing;

import static dev.streamx.cli.util.Output.print;

import dev.streamx.runner.event.ContainerStarted;
import dev.streamx.runner.event.ContainerStopped;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

@ApplicationScoped
public class ContainerWatcher {

  void onContainerStarted(@Observes ContainerStarted event) {
    print("🟢 " + event.getContainerName() + " ready.");
  }

  void onContainerStopped(@Observes ContainerStopped event) {
    print("🔴 " + event.getContainerName() + " stopped.");
  }
}
