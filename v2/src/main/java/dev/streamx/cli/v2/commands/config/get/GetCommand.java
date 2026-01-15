package dev.streamx.cli.v2.commands.config.get;

import dev.streamx.cli.v2.commands.config.list.ListCommand;
import dev.streamx.cli.v2.commands.config.set.SetCommand;
import picocli.CommandLine;

@CommandLine.Command(
  name = "get",
  mixinStandardHelpOptions = true,
  description = "Get configuration property"
)
public class GetCommand {
}
