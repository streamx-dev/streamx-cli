package dev.streamx.cli.ingestion;

import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainLauncher;
import io.quarkus.test.junit.main.QuarkusMainTest;
import org.junit.jupiter.api.Test;

@QuarkusMainTest
public class IngestionArgumentsValidationTest extends BaseIngestionCommandTest {

  private static final String CHANNEL = "pages";
  private static final String KEY = "index.html";

  @Test
  public void shouldRejectIllegalIngestionUrl(QuarkusMainLauncher launcher) {
    // given
    String invalidIngestionUrl = "hattetepe:///in valid";

    // when
    LaunchResult result = launcher.launch("unpublish",
        "--ingestion-url=" + invalidIngestionUrl,
        CHANNEL, KEY);

    // then
    expectError(result,
        "Publication endpoint URI: hattetepe:///in valid/publications/v1 is malformed. "
        + "Illegal character in path");
  }

  @Test
  public void shouldRejectChannellessIngestion(QuarkusMainLauncher launcher) {
    // when
    LaunchResult result = launcher.launch("unpublish");

    // then
    expectError(result, """
        Error: Missing required argument(s): (<channel> <key>)
        Usage: streamx unpublish [-hV] [[--ingestion-url=<propagateIngestionUrl>]]
                                 (<channel> <key>)
        Send unpublication trigger
              <channel>   Channel that message will be published to
              <key>       Message key
          -h, --help      Show this help message and exit.
              --ingestion-url=<propagateIngestionUrl>
                          Address of 'rest-ingestion-service'
                            Default: http://localhost:8080
          -V, --version   Print version information and exit.""");
  }

  @Test
  public void shouldRejectIngestionWithMissingParameter(QuarkusMainLauncher launcher) {
    // when
    LaunchResult result = launcher.launch("publish", "-s");

    // then
    expectError(result, """
        Missing required parameter for option '--string-fragment' (<string>)
        Usage: streamx publish [-hV] [[--ingestion-url=<propagateIngestionUrl>]]
                               (<channel> <key> [payloadFile]) [[[-s=<string> |
                               -b=<binary> | -j=<json>]]...]
        Send publication data
              <channel>       Channel that message will be published to
              <key>           Message key
              [payloadFile]   File containing the payload to be published.
                              This is an optional parameter.
                              This parameter is equivalent to
                              -j=file://<payloadFile> specified before other Payload
                                Defining Option.
                
                              If this parameter is present, it has the highest priority
                                in defining the payload.
          -h, --help          Show this help message and exit.
              --ingestion-url=<propagateIngestionUrl>
                              Address of 'rest-ingestion-service'
                                Default: http://localhost:8080
          -V, --version       Print version information and exit.
                
        Payload Defining Options:
            Payload can be defined by specifying an explicit full payload.
            For example, publish -j "{}" (...)
            will send {} to the ingestion service.
                
            A payload can also be created by specifying one or more
            JSONPath expressions with payload fragments.
            For example, publish -s type=string -s content.bytes=hello (...)
            will send {"type":"string","content":{"bytes":"hello"}}
            to the ingestion service.
                
            There are few ways to define the parameter value:
            * if value has prefix file:// then value will be loaded
              from file with given (relative or absolute) path
            * otherwise raw value will be used as value
                
            Payload fragment type:
            * if the value is a string fragment, use -s option
            * if the value is a JSON fragment, use -j option
            * if the value is a binary value fragment use -b option
                
          -b, --binary-fragment=<binary>
                              Defines a payload fragment of binary type
          -j, --json-fragment=<json>
                              Defines a payload fragment of JSON node type
          -s, --string-fragment=<string>
                              Defines a payload fragment of string type""");
  }

  @Test
  public void shouldRejectKeylessIngestion(QuarkusMainLauncher launcher) {
    // when
    LaunchResult result = launcher.launch("unpublish", "channel");

    // then
    expectError(result, """
        Error: Missing required argument(s): <key>
        Usage: streamx unpublish [-hV] [[--ingestion-url=<propagateIngestionUrl>]]
                                 (<channel> <key>)
        Send unpublication trigger
              <channel>   Channel that message will be published to
              <key>       Message key
          -h, --help      Show this help message and exit.
              --ingestion-url=<propagateIngestionUrl>
                          Address of 'rest-ingestion-service'
                            Default: http://localhost:8080
          -V, --version   Print version information and exit.""");
  }

  @Test
  public void shouldRejectInvalidHostInIngestionUrl(QuarkusMainLauncher launcher) {
    // given
    String invalidIngestionUrl = "hattetepe:///invalid";

    // when
    LaunchResult result = launcher.launch("unpublish",
        "--ingestion-url=" + invalidIngestionUrl,
        CHANNEL, KEY);

    // then
    expectError(result,
        "Publication endpoint URI: hattetepe:///invalid/publications/v1 is malformed. "
        + "URI without host is not supported.");
  }

  @Override
  protected void initializeWiremock() {
    // no mock responses to configure for this test
  }
}
