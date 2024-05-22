package dev.streamx.cli.licence;

import static dev.streamx.cli.util.ExceptionUtils.sneakyThrow;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.streamx.cli.exception.LicenceException;
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
class LicenceFetcher {

  @ConfigProperty(name = "streamx.cli.licence.current-licence-url")
  String licenceUrl;

  @ConfigProperty(name = "streamx.cli.licence.timeout", defaultValue = "5000")
  int licenceFetchTimeout;

  @Inject
  @LicenceProcessing
  ObjectMapper objectMapper;

  @Inject
  CloseableHttpClient httpClient;

  public Licence fetchCurrentLicence() {
    Licences licences = fetchLicences();

    return Optional.ofNullable(licences.streamxCli())
        .orElseGet(licences::defaultLicence);
  }

  private Licences fetchLicences() {
    URI licenceUrl = buildLicenceUrl();
    HttpGet httpRequest = prepareRequest(licenceUrl);
    byte[] byteArray = fetchLicenceRawContent(httpRequest, licenceUrl);
    return parseContent(byteArray, licenceUrl);
  }

  @NotNull
  private HttpGet prepareRequest(URI licenceUrl) {
    HttpGet httpRequest = new HttpGet(licenceUrl);
    httpRequest.setConfig(RequestConfig.copy(RequestConfig.DEFAULT)
            .setConnectTimeout(licenceFetchTimeout)
            .setSocketTimeout(licenceFetchTimeout)
        .build());
    return httpRequest;
  }

  @NotNull
  private Licences parseContent(byte[] byteArray, URI licenceUrl) {
    try {
      return Optional.ofNullable(objectMapper.readValue(byteArray, StreamxLicencesYaml.class))
          .map(StreamxLicencesYaml::streamxCli)
          .orElseThrow();
    } catch (IOException | NoSuchElementException e) {
      throw LicenceException.malformedLicenceException(licenceUrl);
    }
  }

  private byte[] fetchLicenceRawContent(HttpGet httpRequest, URI licenceUrl) {
    byte[] byteArray;
    try (CloseableHttpResponse response = httpClient.execute(httpRequest)) {
      byteArray = EntityUtils.toByteArray(response.getEntity());
    } catch (IOException e) {
      throw LicenceException.licenceFetchException(licenceUrl);
    }
    return byteArray;
  }

  @NotNull
  private URI buildLicenceUrl() {
    try {
      return new URI(this.licenceUrl);
    } catch (URISyntaxException e) {
      throw sneakyThrow(e);
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record StreamxLicencesYaml(
      @JsonProperty("licences") Licences streamxCli
  ) { }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Licences(
      @JsonProperty("streamx-cli") Licence streamxCli,
      @JsonProperty("default") Licence defaultLicence
  ) { }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Licence(
      @JsonProperty("name") String name,
      @JsonProperty("url") String url
  ) { }
}
