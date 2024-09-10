package dev.streamx.cli;

import dev.streamx.cli.config.ArgumentConfigSource;
import dev.streamx.cli.config.validation.ConfigSourcesValidator;
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
import org.jetbrains.annotations.Nullable;
import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;
import picocli.CommandLine.ParseResult;

@QuarkusMain(name = "StreamX CLI Main")
@TopCommand
@Command(mixinStandardHelpOptions = true,
    name = "streamx",
    subcommands = {RunCommand.class, PublishCommand.class, UnpublishCommand.class,
        HelpCommand.class},
    versionProvider = VersionProvider.class)
public class StreamxCommand implements QuarkusApplication {

  @Inject
  CommandLine.IFactory factory;

  @Inject
  ParameterExceptionHandler parameterExceptionHandler;

  @Inject
  ExecutionExceptionHandler executionExceptionHandler;

  @Inject
  LicenseProcessorEntrypoint licenseProcessorEntrypoint;

  @Inject
  ConfigSourcesValidator configSourcesValidator;

  @Inject
  BannerPrinter bannerPrinter;

  @ArgGroup(exclusive = false)
  LicenseArguments licenseArguments;

  private CommandLine commandLine;
  private String[] args;

  @Override
  public int run(String... args) throws Exception {
    this.args = args;

    commandLine = new CommandLine(this, factory)
        .setParameterExceptionHandler(parameterExceptionHandler)
        .setExecutionExceptionHandler(executionExceptionHandler)
        .setExpandAtFiles(false)
        .setExecutionStrategy(this::executionStrategy);

    Integer x = validateProperties();
    if (x != null) {
      return x;
    }
    return commandLine.execute(args);
  }

  @Nullable
  private Integer validateProperties() {
    try {
      configSourcesValidator.validate();
    } catch (Exception e) {
      executionExceptionHandler.handleExecutionException(e, commandLine,
          commandLine.getParseResult());
      return 1;
    }
    return null;
  }

  private int executionStrategy(ParseResult parseResult) {
    try {
      init();

      return new CommandLine.RunLast().execute(parseResult);
    } catch (Exception e) {
      executionExceptionHandler.handleExecutionException(e, commandLine, parseResult);
      return 1;
    }
  }

  private void init() {
    bannerPrinter.initialize(commandLine, args);

    licenseProcessorEntrypoint.process();
  }

  public static void main(String... args) {
    initializeArgumentConfigSource(args);

    Quarkus.run(StreamxCommand.class, args);
  }

  private static void initializeArgumentConfigSource(String[] args) {
    try {
      new CommandLine(new StreamxCommand()).parseArgs(args);
    } catch (Exception e) {
      // Parsing args exception will be handled when Quarkus Context is up
      // to provide uniform exception handling
    } finally {
      ArgumentConfigSource.lock();
    }
  }
}