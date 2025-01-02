package dev.streamx.cli.command.cloud.undeploy;

import static dev.streamx.cli.util.Output.printf;

import dev.streamx.cli.VersionProvider;
import dev.streamx.cli.command.cloud.KubernetesArguments;
import dev.streamx.cli.command.cloud.KubernetesService;
import dev.streamx.cli.command.cloud.deploy.DeployCommand;
import jakarta.inject.Inject;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;

@Command(
    name = UndeployCommand.COMMAND_NAME,
    mixinStandardHelpOptions = true,
    versionProvider = VersionProvider.class,
    description = "Undeploys the StreamX Project from the cloud. "
        + DeployCommand.CLOUD_COMMAND_DESCRIPTION
)
public class UndeployCommand implements Runnable {

  public static final String COMMAND_NAME = "undeploy";

  @ArgGroup
  KubernetesArguments namespaceArg;

  @Inject
  KubernetesService kubernetesService;

  @Override
  public void run() {
    kubernetesService.validateCrdInstallation();
    kubernetesService.undeploy();
    printf("StreamX project successfully undeployed from '%s' namespace.",
        kubernetesService.getNamespace());
  }
}
