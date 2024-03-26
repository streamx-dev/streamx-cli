package dev.streamx.cli;

import dev.streamx.cli.ingestion.publish.PublishCommand;
import dev.streamx.cli.run.RunCommand;
import dev.streamx.cli.ingestion.unpublish.UnpublishCommand;
import io.quarkus.picocli.runtime.annotations.TopCommand;
import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import jakarta.inject.Inject;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@QuarkusMain(name = "StreamX CLI Main")
@TopCommand
@Command(mixinStandardHelpOptions = true,
    subcommands = {RunCommand.class, PublishCommand.class, UnpublishCommand.class})
public class StreamxCommand implements QuarkusApplication {

  @Inject
  CommandLine.IFactory factory;

  @Inject
  ExceptionHandler exceptionHandler;

  @Override
  public int run(String... args) throws Exception {
    return new CommandLine(this, factory)
        .setExecutionExceptionHandler(exceptionHandler)
        .setExpandAtFiles(false)
        .execute(args);
  }

  public static void main(String... args) {
    Quarkus.run(StreamxCommand.class, args);
  }
}