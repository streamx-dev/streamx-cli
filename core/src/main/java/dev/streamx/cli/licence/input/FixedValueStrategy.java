package dev.streamx.cli.licence.input;

public class FixedValueStrategy implements AcceptingStrategy {
  private final boolean fixedValue;

  public FixedValueStrategy(boolean fixedValue) {
    this.fixedValue = fixedValue;
  }

  @Override
  public boolean isLicenceAccepted() {
    return fixedValue;
  }
}
