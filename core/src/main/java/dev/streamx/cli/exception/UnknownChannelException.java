package dev.streamx.cli.exception;

public class UnknownChannelException extends Exception {

  private final String channel;
  private final String availableChannels;

  public UnknownChannelException(String channel, String availableChannels) {
    this.channel = channel;
    this.availableChannels = availableChannels;
  }

  public String getChannel() {
    return channel;
  }

  public String getAvailableChannels() {
    return availableChannels;
  }
}
