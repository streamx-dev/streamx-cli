package dev.streamx.cli;

import dev.streamx.cli.test.tools.terminal.command.CmdCommandStrategy;
import dev.streamx.cli.test.tools.terminal.command.OsCommandStrategy;
import dev.streamx.cli.test.tools.terminal.command.ShellCommandStrategy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Dependent
public class Config {

  @ApplicationScoped
  public CloseableHttpClient httpClient() {
    return HttpClients.createDefault();
  }

  @ApplicationScoped
  public OsCommandStrategy strategy(@ConfigProperty(name = "os.name") String osName) {
    if (osName.toLowerCase().contains("win")) {
      return new CmdCommandStrategy();
    }
    return new ShellCommandStrategy();
  }

  @ApplicationScoped
  public StreamxTerminalCommandProducer streamxTerminalCommandProducer(
      @ConfigProperty(name = "streamx.cli.e2e.streamxCommandType", defaultValue = "built")
      String streamxCommandType
  ) {
    return new StreamxTerminalCommandProducer(streamxCommandType);
  }

  @ApplicationScoped
  public StreamxTerminalCommand streamxTerminalCommand(StreamxTerminalCommandProducer producer) {
    return producer.produce();
  }
}
