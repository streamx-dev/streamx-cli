package dev.streamx.cli.interpolation;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.io.IOContext;
import java.io.IOException;
import java.io.Reader;

public class InterpolatingJsonFactory extends JsonFactory {

  private final InterpolationSupport interpolationSupport;

  public InterpolatingJsonFactory(InterpolationSupport interpolationSupport) {
    this.interpolationSupport = interpolationSupport;
  }

  @Override
  protected JsonParser _createParser(char[] data, int offset, int length, IOContext context,
      boolean recyclable) throws IOException {
    return new InterpolatingJsonParser(
        super._createParser(data, offset, length, context, recyclable), interpolationSupport);
  }

  @Override
  protected JsonParser _createParser(Reader reader, IOContext context) throws IOException {
    return new InterpolatingJsonParser(super._createParser(reader, context), interpolationSupport);
  }
}
