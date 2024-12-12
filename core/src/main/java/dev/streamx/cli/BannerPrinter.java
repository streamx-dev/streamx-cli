package dev.streamx.cli;

import static dev.streamx.cli.util.Output.print;

import dev.streamx.cli.command.manage.ManageCommand;
import dev.streamx.cli.command.run.RunCommand;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Set;
import picocli.CommandLine;
import picocli.CommandLine.ParseResult;

@ApplicationScoped
public class BannerPrinter {

  private static final Set<String> COMMANDS_REQUIRING_PRINTING_BANNER =
      Set.of(RunCommand.COMMAND_NAME, ManageCommand.COMMAND_NAME);

  private static final String BANNER = """
       ____  _                           __  __
      / ___|| |_ _ __ ___  __ _ _ __ ___ \\ \\/ /
      \\___ \\| __| '__/ _ \\/ _` | '_ ` _ \\ \\  /\s
       ___) | |_| | |  __/ (_| | | | | | |/  \\\s
      |____/ \\__|_|  \\___|\\__,_|_| |_| |_/_/\\_\\.dev
                                               \s""";

  private boolean bannerShouldBePrinted = false;
  private boolean bannerAlreadyPrinted = false;

  void initialize(CommandLine commandLine, String[] args) {
    ParseResult parseResult = commandLine.parseArgs(args);

    ParseResult subcommand = parseResult.subcommand();
    if (subcommand == null) {
      return;
    }

    String commandName = subcommand.commandSpec().name();
    if (COMMANDS_REQUIRING_PRINTING_BANNER.contains(commandName)) {
      bannerShouldBePrinted = true;
    }
  }

  public void printBanner() {
    if (bannerShouldBePrinted && !bannerAlreadyPrinted) {
      print(BANNER);
      bannerAlreadyPrinted = true;
    }
  }
}
