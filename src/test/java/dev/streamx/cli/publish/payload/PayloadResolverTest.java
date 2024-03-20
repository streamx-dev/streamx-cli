package dev.streamx.cli.publish.payload;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class PayloadResolverTest {

  private static final JsonNode EXAMPLE_JSON_NODE;
  static {
    try {
      EXAMPLE_JSON_NODE = new ObjectMapper().readTree("{\"nana\": \"lele\"}");
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  PayloadResolver cut = new PayloadResolver();

  @Test
  void shouldValidateData() {
    // given
    String corruptedArg = """
        {"nana": " """;

    // when
    Exception exception = catchException(() -> cut.createPayload(corruptedArg));

    // then
    assertThat(exception).isInstanceOf(PayloadException.class);
    assertThat(exception).hasMessageContaining("Payload could not be parsed.");
  }

  @Test
  void shouldValidatePath() {
    // given
    String corruptedArg = "@nonexisting";

    // when
    Exception exception = catchException(() -> cut.createPayload(corruptedArg));

    // then
    assertThat(exception).isInstanceOf(PayloadException.class);
    assertThat(exception).hasMessageContaining("File do not exists.");
  }

  @Test
  void shouldValidateFileContent() {
    // given
    String corruptedPathArg = "@target/test-classes/dev/streamx/cli/publish/payload/invalid-payload.json";

    // when
    Exception exception = catchException(() -> cut.createPayload(corruptedPathArg));

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
    JsonNode payload = cut.createPayload(arg);

    // then
    assertThat(payload).isEqualTo(EXAMPLE_JSON_NODE);
  }

  @Test
  void shouldExtractDataFromFileAndConvertToJsonNode() {
    // given
    String arg = "@target/test-classes/dev/streamx/cli/publish/payload/payload.json";

    // when
    JsonNode payload = cut.createPayload(arg);

    // then
    assertThat(payload).isEqualTo(EXAMPLE_JSON_NODE);
  }
}