package dev.streamx.cli.command.ingestion;

import static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.responseDefinition;
import static com.github.tomakehurst.wiremock.common.ContentTypes.APPLICATION_JSON;
import static com.github.tomakehurst.wiremock.common.ContentTypes.CONTENT_TYPE;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static dev.streamx.clients.ingestion.StreamxClient.INGESTION_ENDPOINT_PATH_V1;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import io.quarkus.test.junit.main.LaunchResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;

public abstract class BaseIngestionCommandTest {

  @RegisterExtension
  protected static WireMockExtension wm = WireMockExtension.newInstance()
      .options(wireMockConfig().dynamicPort())
      .configureStaticDsl(true)
      .build();

  @BeforeEach
  void setup() {
    initializeWiremock();
  }

  protected abstract void initializeWiremock();

  protected static String getIngestionUrl() {
    return "http://localhost:" + wm.getPort();
  }

  protected static String getPublicationPath(String channel) {
    return INGESTION_ENDPOINT_PATH_V1 + "/channels/" + channel + "/messages";
  }

  protected static String getChannelsPath() {
    return INGESTION_ENDPOINT_PATH_V1 + "/channels";
  }

  protected static void expectSuccess(LaunchResult result) {
    assertThat(result.exitCode()).isZero();
    assertThat(result.getErrorOutput()).isEmpty();
  }

  protected static void expectError(LaunchResult result, String expectedErrorOutput) {
    assertThat(result.exitCode()).isNotZero();
    assertThat(result.getErrorOutput()).contains(expectedErrorOutput);
  }

  protected static void setupMockChannelsSchemasResponse() {
    ResponseDefinitionBuilder mockResponse = responseDefinition()
        .withStatus(200)
        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
        .withBody(
            """
                {
                  "pages": {
                    "type": "record",
                    "name": "DataIngestionMessage",
                    "namespace": "dev.streamx.ingestion.rest.test",
                    "fields": [
                      {
                        "name": "key",
                        "type": "string"
                      },
                      {
                        "name": "action",
                        "type": "string"
                      },
                      {
                        "name": "eventTime",
                        "type": [
                          "null",
                          "long"
                        ]
                      },
                      {
                        "name": "properties",
                        "type": {
                          "type": "map",
                          "values": "string"
                        }
                      },
                      {
                        "name": "payload",
                        "type": [
                          "null",
                          {
                            "type": "record",
                            "name": "Page",
                            "namespace": "dev.streamx.blueprints.data",
                            "fields": [
                              {
                                "name": "content",
                                "type": [
                                  "null",
                                  "bytes"
                                ],
                                "default": null
                              }
                            ]
                          }
                        ]
                      }
                    ]
                  }
                }
                """
        );

    wm.stubFor(WireMock.get(getChannelsPath())
        .willReturn(mockResponse));
  }
}
