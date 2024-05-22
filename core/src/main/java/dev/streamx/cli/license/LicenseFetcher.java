package dev.streamx.cli.license;

import static dev.streamx.cli.util.ExceptionUtils.sneakyThrow;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.streamx.cli.exception.LicenseException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jetbrains.annotations.NotNull;

@ApplicationScoped
class LicenseFetcher {

  @ConfigProperty(name = "streamx.cli.license.current-license-url")
  String licenseUrl;

  @ConfigProperty(name = "streamx.cli.license.timeout", defaultValue = "5000")
  int licenseFetchTimeout;

  @Inject
  @LicenseProcessing
  ObjectMapper objectMapper;

  @Inject
  CloseableHttpClient httpClient;

  public License fetchCurrentLicense() {
    Licenses licenses = fetchLicenses();

    return Optional.ofNullable(licenses.streamxCli())
        .orElseGet(licenses::defaultLicense);
  }

  private Licenses fetchLicenses() {
    URI licenseUrl = buildLicenseUrl();
    HttpGet httpRequest = prepareRequest(licenseUrl);
    byte[] byteArray = fetchLicenseRawContent(httpRequest, licenseUrl);
    return parseContent(byteArray, licenseUrl);
  }

  @NotNull
  private HttpGet prepareRequest(URI licenseUrl) {
    HttpGet httpRequest = new HttpGet(licenseUrl);
    httpRequest.setConfig(RequestConfig.copy(RequestConfig.DEFAULT)
            .setConnectTimeout(licenseFetchTimeout)
            .setSocketTimeout(licenseFetchTimeout)
        .build());
    return httpRequest;
  }

  @NotNull
  private Licenses parseContent(byte[] byteArray, URI licenseUrl) {
    try {
      return Optional.ofNullable(objectMapper.readValue(byteArray, StreamxLicensesYaml.class))
          .map(StreamxLicensesYaml::streamxCli)
          .orElseThrow();
    } catch (IOException | NoSuchElementException e) {
      throw LicenseException.malformedLicenseException(licenseUrl);
    }
  }

  private byte[] fetchLicenseRawContent(HttpGet httpRequest, URI licenseUrl) {
    byte[] byteArray;
    try (CloseableHttpResponse response = httpClient.execute(httpRequest)) {
      byteArray = EntityUtils.toByteArray(response.getEntity());
    } catch (IOException e) {
      throw LicenseException.licenseFetchException(licenseUrl);
    }
    return byteArray;
  }

  @NotNull
  private URI buildLicenseUrl() {
    try {
      return new URI(this.licenseUrl);
    } catch (URISyntaxException e) {
      throw sneakyThrow(e);
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record StreamxLicensesYaml(
      @JsonProperty("licenses") Licenses streamxCli
  ) { }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Licenses(
      @JsonProperty("streamx-cli") License streamxCli,
      @JsonProperty("default") License defaultLicense
  ) { }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record License(
      @JsonProperty("name") String name,
      @JsonProperty("url") String url
  ) { }
}
