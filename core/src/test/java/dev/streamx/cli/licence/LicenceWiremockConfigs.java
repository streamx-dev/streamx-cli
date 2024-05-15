package dev.streamx.cli.licence;

import static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.responseDefinition;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.common.ContentTypes.CONTENT_TYPE;
import static org.apache.hc.core5.http.HttpStatus.SC_OK;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

class LicenceWiremockConfigs {

  public static class StandardWiremockLicence extends AbstractWiremockLicences {

    @Override
    public Map<String, String> start() {
      String response = """
          licences:
              streamx-cli:
                  name: "EULA"
                  url: "http://fake.streamx.dev/eula.html"
              default:
                  name: "Apachee"
                  url: "http://fake.streamx.dev/apachee.html"
          """;
      int delayMillis = 0;

      return startServer(response, delayMillis);
    }
  }

  public static class TimeoutWiremockLicence extends AbstractWiremockLicences {

    @Override
    public Map<String, String> start() {
      String response = """
          # Copyright (c) Dynamic Solutions. All rights reserved.
          licences:
              streamx-cli:
                  name: "StreamX End-User License Agreement 1.0"
                  url: "https://www.streamx.dev/licenses/eula-v1-0.html"
              default:
                  name: "StreamX End-User License Agreement 1.0"
                  url: "https://www.streamx.dev/licenses/eula-v1-0.html"
          """;
      int delayMillis = 50;

      Map<String, String> resultConfig = startServer(response, delayMillis);
      resultConfig.put("streamx.cli.licence.timeout", "10");

      return resultConfig;
    }
  }

  public static class MalformedWiremockLicence extends AbstractWiremockLicences {

    @Override
    public Map<String, String> start() {
      String response = """
          # Copyright (c) Dynamic Solutions. All rights reserved.
          licences:
              streamx-cli:
                  name: "StreamX End-User License Agreement 1.0"
                  url: "http
          """;
      return startServer(response, 0);
    }
  }

  public abstract static class AbstractWiremockLicences
      implements QuarkusTestResourceLifecycleManager {

    private WireMockServer wireMockServer;

    protected Map<String, String> startServer(String responseBody, int delayMillis) {
      wireMockServer = new WireMockServer(0);
      wireMockServer.start();
      wireMockServer.stubFor(get("/licences.yaml")
          .willReturn(responseDefinition().withStatus(SC_OK).withBody(responseBody)
              .withFixedDelay(delayMillis)
              .withHeader(CONTENT_TYPE, "text/yaml")
          )
      );

      Map<String, String> result = new HashMap<>();
      result.put("streamx.cli.licence.current-licence-url",
          mockedLicenceBaseUrl(wireMockServer.port()));
      return result;
    }

    @Override
    public void stop() {
      if (null != wireMockServer) {
        wireMockServer.stop();
      }
    }
  }

  @NotNull
  private static String mockedLicenceBaseUrl(int port) {
    return "http://localhost:" + port + "/licences.yaml";
  }
}