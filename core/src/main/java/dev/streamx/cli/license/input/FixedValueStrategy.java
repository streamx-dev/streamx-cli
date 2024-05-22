package dev.streamx.cli.license.input;

public class FixedValueStrategy implements AcceptingStrategy {
  private final boolean fixedValue;

  public FixedValueStrategy(boolean fixedValue) {
    this.fixedValue = fixedValue;
  }

  @Override
  public boolean isLicenseAccepted() {
    return fixedValue;
  }
}
