package dev.streamx.cli;

import dev.streamx.cli.test.tools.terminal.command.CmdCommandStrategy;
import dev.streamx.cli.test.tools.terminal.command.OsCommandStrategy;
import dev.streamx.cli.test.tools.terminal.command.ShellCommandStrategy;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class Config {

  @ConfigProperty(name = "os.name")
  String osName;

  @ApplicationScoped
  public CloseableHttpClient httpClient() {
    return HttpClients.createDefault();
  }

  @ApplicationScoped
  public OsCommandStrategy strategy() {
    if (osName.contains("win")) {
      return new CmdCommandStrategy();
    }
    return new ShellCommandStrategy();
  }

  @ApplicationScoped
  public StreamxCommand streamxCommand(StreamxCommandProducer producer) {
    return producer.produce();
  }
}
