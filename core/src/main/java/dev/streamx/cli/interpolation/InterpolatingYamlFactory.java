package dev.streamx.cli.interpolation;

import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLParser;
import java.io.CharArrayReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

public class InterpolatingYamlFactory extends YAMLFactory {

  private final InterpolationSupport interpolationSupport;

  public InterpolatingYamlFactory(InterpolationSupport interpolationSupport) {
    this.interpolationSupport = interpolationSupport;
  }

  @Override
  protected YAMLParser _createParser(InputStream in, IOContext context) throws IOException {
    return new InterpolatingYamlParser(context, this._parserFeatures, this._yamlParserFeatures,
        this._loaderOptions, this._objectCodec, this._createReader(in, null, context),
        interpolationSupport);
  }

  @Override
  protected YAMLParser _createParser(Reader r, IOContext context) {
    return new InterpolatingYamlParser(context, this._parserFeatures, this._yamlParserFeatures,
        this._loaderOptions, this._objectCodec, r, interpolationSupport);
  }

  @Override
  protected YAMLParser _createParser(char[] data, int offset, int len, IOContext context,
      boolean recyclable) {
    return new InterpolatingYamlParser(context, this._parserFeatures, this._yamlParserFeatures,
        this._loaderOptions, this._objectCodec, new CharArrayReader(data, offset, len),
        interpolationSupport);
  }

  @Override
  protected YAMLParser _createParser(byte[] data, int offset, int len, IOContext context)
      throws IOException {
    return new InterpolatingYamlParser(context, this._parserFeatures, this._yamlParserFeatures,
        this._loaderOptions, this._objectCodec,
        this._createReader(data, offset, len, null, context), interpolationSupport);
  }
}
