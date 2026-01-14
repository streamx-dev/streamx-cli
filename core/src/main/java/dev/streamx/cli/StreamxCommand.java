package dev.streamx.cli;

import dev.streamx.cli.command.cloud.deploy.DeployCommand;
import dev.streamx.cli.command.cloud.undeploy.UndeployCommand;
import dev.streamx.cli.command.dev.DevCommand;
import dev.streamx.cli.command.ingestion.batch.BatchCommand;
import dev.streamx.cli.command.ingestion.publish.PublishCommand;
import dev.streamx.cli.command.ingestion.stream.StreamCommand;
import dev.streamx.cli.command.ingestion.unpublish.UnpublishCommand;
import dev.streamx.cli.command.init.InitCommand;
import dev.streamx.cli.command.run.RunCommand;
import dev.streamx.cli.config.ArgumentConfigSource;
import dev.streamx.cli.config.validation.ConfigSourcesValidator;
import dev.streamx.cli.license.LicenseArguments;
import dev.streamx.cli.license.LicenseProcessorEntrypoint;
import io.quarkus.picocli.runtime.annotations.TopCommand;
import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import jakarta.inject.Inject;
import java.text.SimpleDateFormat;
import java.util.Date;
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
    subcommands = {
        InitCommand.class,
        RunCommand.class, DevCommand.class,
        PublishCommand.class, UnpublishCommand.class,
        BatchCommand.class, StreamCommand.class,
        DeployCommand.class, UndeployCommand.class,
        HelpCommand.class
    },
    versionProvider = VersionProvider.class)
public class StreamxCommand implements QuarkusApplication {
  private static final SimpleDateFormat DATE_FORMAT =
          new SimpleDateFormat("yyyy_MM_dd__HH_mm_ss_SSS");
  private static final String LOG_FILE_PATH_PROPERTY_NAME = "%prod.quarkus.log.file.path";
  private static final String STREAMX_LOG_FILE_NAME_PATTERN = "%s/.streamx/logs/streamx-%s.log";

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

  public static void main(String... args) {
    initializeArgumentConfigSource(args);

    overrideLogFileName();

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

  private static void overrideLogFileName() {
    if (System.getProperty(LOG_FILE_PATH_PROPERTY_NAME) != null) {
      return;
    }

    String userHome = System.getProperty("user.home");
    String date = DATE_FORMAT.format(new Date());
    String streamxLogPath = String.format(STREAMX_LOG_FILE_NAME_PATTERN, userHome, date);

    System.setProperty(LOG_FILE_PATH_PROPERTY_NAME, streamxLogPath);
  }

  private void init() {
    bannerPrinter.initialize(commandLine, args);

    licenseProcessorEntrypoint.process();
  }
}
