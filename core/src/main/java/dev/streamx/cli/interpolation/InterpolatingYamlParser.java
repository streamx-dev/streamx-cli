package dev.streamx.cli.interpolation;

import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.dataformat.yaml.YAMLParser;
import java.io.IOException;
import java.io.Reader;
import org.yaml.snakeyaml.LoaderOptions;

public class InterpolatingYamlParser extends YAMLParser {

  private final InterpolationSupport interpolationSupport;

  public InterpolatingYamlParser(IOContext context, int parserFeatures, int formatFeatures,
      LoaderOptions loaderOptions, ObjectCodec codec, Reader reader,
      InterpolationSupport interpolationSupport) {
    super(context, parserFeatures, formatFeatures, loaderOptions, codec, reader);
    this.interpolationSupport = interpolationSupport;
  }

  @Override
  public String getText() throws IOException {
    final String value = super.getText();
    if (value != null) {
      return interpolationSupport.expand(value);
    }
    return null;
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
