package dev.streamx.cli.command.meshprocessing;

import static dev.streamx.cli.util.Output.print;

import com.streamx.runner.event.ContainerFailed;
import com.streamx.runner.event.ContainerStarted;
import com.streamx.runner.event.ContainerStopped;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

@ApplicationScoped
public class ContainerWatcher {

  void onContainerStarted(@Observes ContainerStarted event) {
    print("🟢 " + event.containerName() + " ready.");
  }

  void onContainerStopped(@Observes ContainerStopped event) {
    print("🔴 " + event.containerName() + " stopped.");
  }

  void onContainerFailed(@Observes ContainerFailed event) {
    print("❌ " + event.containerName() + " failed.");
  }
}
