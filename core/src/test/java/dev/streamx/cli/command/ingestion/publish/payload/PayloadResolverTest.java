package dev.streamx.cli.command.ingestion.publish.payload;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.streamx.cli.command.ingestion.publish.PayloadArgument;
import dev.streamx.cli.exception.JsonPathReplacementException;
import dev.streamx.cli.exception.PayloadException;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.List;
import org.junit.jupiter.api.Test;

@QuarkusTest
class PayloadResolverTest {

  private static final JsonNode EXAMPLE_JSON_NODE;
  private static final String TEST_RESOURCES = "file://target/test-classes";

  static {
    try {
      EXAMPLE_JSON_NODE = new ObjectMapper().readTree("{\"nana\": \"lele\"}");
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  @Inject
  PayloadResolver cut;

  @Test
  void shouldValidateMissingPayload() {
    // when
    Exception exception = catchException(() -> cut.createPayload(List.of()));

    // then
    assertThat(exception).isInstanceOf(PayloadException.class);
    assertThat(exception).hasMessageContaining("Payload definition not found.");
  }

  @Test
  void shouldValidateData() {
    // given
    String corruptedArg = """
        {"nana": " """;

    // when
    Exception exception = catchException(() -> createPayload(corruptedArg));

    // then
    assertThat(exception).isInstanceOf(PayloadException.class);
    assertThat(exception).hasMessageContaining("Payload could not be parsed.");
  }

  @Test
  void shouldValidatePath() {
    // given
    String corruptedArg = "file://nonexisting";

    // when
    Exception exception = catchException(() -> createPayload(corruptedArg));

    // then
    assertThat(exception).isInstanceOf(PayloadException.class);
    assertThat(exception).hasMessageContaining("File does not exist.");
  }

  @Test
  void shouldValidateFileContent() {
    // given
    String corruptedPathArg =
        TEST_RESOURCES + "/dev/streamx/cli/command/ingestion/publish/payload/invalid-payload.json";

    // when
    Exception exception = catchException(() -> createPayload(corruptedPathArg));

    // then
    assertThat(exception).isInstanceOf(PayloadException.class);
    assertThat(exception).hasMessageContaining("Payload could not be parsed.");
  }

  @Test
  void shouldConvertDirectArgToJsonNode() {
    // given
    String arg = """
        {"nana": "lele"}""";

    // when
    JsonNode payload = createPayload(arg);

    // then
    assertThat(payload).isEqualTo(EXAMPLE_JSON_NODE);
  }

  @Test
  void shouldExtractDataFromFileAndConvertToJsonNode() {
    // given
    String arg = TEST_RESOURCES + "/dev/streamx/cli/command/ingestion/publish/payload/payload.json";

    // when
    JsonNode payload = createPayload(arg);

    // then
    assertThat(payload).isEqualTo(EXAMPLE_JSON_NODE);
  }

  @Test
  void shouldValidateMissingJsonPathOfNonJsonNodePayload() {
    // given
    String arg = TEST_RESOURCES + "/dev/streamx/cli/command/ingestion/publish/payload/payload.json";

    // when
    Exception exception = catchException(() ->
        cut.createPayload(List.of(
            PayloadArgument.ofJsonNode(arg),
            PayloadArgument.ofBinary("$..ana"))
        )
    );

    // then
    assertThat(exception).isInstanceOf(JsonPathReplacementException.class);
  }

  @Test
  void shouldValidateNonexistingJsonPathOfNonJsonNodePayload() {
    // given
    String arg = TEST_RESOURCES + "/dev/streamx/cli/command/ingestion/publish/payload/payload.json";

    // when
    Exception exception = catchException(() ->
        cut.createPayload(List.of(
            PayloadArgument.ofJsonNode(arg),
            PayloadArgument.ofBinary("$..ana=lele"))
        )
    );

    // then
    assertThat(exception).isInstanceOf(JsonPathReplacementException.class);
  }

  public JsonNode createPayload(String payload) {
    return cut.createPayload(List.of(PayloadArgument.ofJsonNode(payload)));
  }
}
