package dev.streamx.cli.interpolation;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.util.JsonParserDelegate;
import java.io.IOException;

public class InterpolatingJsonParser extends JsonParserDelegate {

  private final InterpolationSupport interpolationSupport;

  public InterpolatingJsonParser(final JsonParser delegate,
      InterpolationSupport interpolationSupport) {
    super(delegate);
    this.interpolationSupport = interpolationSupport;
  }

  @Override
  public String getText() throws IOException {
    final String value = super.getText();
    if (value != null) {
      return interpolationSupport.expand(value);
    }
    return value;
  }

  @Override
  public String getValueAsString() throws IOException {
    return getValueAsString(null);
  }

  @Override
  public String getValueAsString(final String defaultValue) throws IOException {
    final String value = super.getValueAsString(defaultValue);
    if (value != null) {
      return interpolationSupport.expand(value);
    }
    return null;
  }
}
