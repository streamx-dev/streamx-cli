package dev.streamx.cli;

import dev.streamx.cli.ingestion.publish.PublishCommand;
import dev.streamx.cli.ingestion.unpublish.UnpublishCommand;
import dev.streamx.cli.license.LicenseArguments;
import dev.streamx.cli.license.LicenseProcessorEntrypoint;
import dev.streamx.cli.run.RunCommand;
import io.quarkus.picocli.runtime.annotations.TopCommand;
import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import jakarta.inject.Inject;
import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParseResult;

@QuarkusMain(name = "StreamX CLI Main")
@TopCommand
@Command(mixinStandardHelpOptions = true,
    name = "streamx",
    subcommands = {RunCommand.class, PublishCommand.class, UnpublishCommand.class},
    versionProvider = VersionProvider.class)
public class StreamxCommand implements QuarkusApplication {

  @Inject
  CommandLine.IFactory factory;

  @Inject
  ExceptionHandler exceptionHandler;

  @Inject
  LicenseProcessorEntrypoint licenseProcessorEntrypoint;

  @ArgGroup(exclusive = false)
  LicenseArguments licenseArguments;
  private CommandLine commandLine;

  @Override
  public int run(String... args) throws Exception {
    commandLine = new CommandLine(this, factory)
        .setExecutionExceptionHandler(exceptionHandler)
        .setExpandAtFiles(false)
        .setExecutionStrategy(this::executionStrategy);
    return commandLine.execute(args);
  }

  private int executionStrategy(ParseResult parseResult) {
    try {
      init();

      return new CommandLine.RunLast().execute(parseResult);
    } catch (Exception e) {
      exceptionHandler.handleExecutionException(e, commandLine, parseResult);
      return 1;
    }
  }

  private void init() {
    licenseProcessorEntrypoint.process();
  }

  public static void main(String... args) {
    Quarkus.run(StreamxCommand.class, args);
  }
}