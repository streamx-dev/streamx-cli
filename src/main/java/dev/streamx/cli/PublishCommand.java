package dev.streamx.cli;

import picocli.CommandLine.Command;

@Command(name = "publish", mixinStandardHelpOptions = true)
public class PublishCommand implements Runnable {

  @Override
  public void run() {
    System.out.print("Publishing!");
  }

}
