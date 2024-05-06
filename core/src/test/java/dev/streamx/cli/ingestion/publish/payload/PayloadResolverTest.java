package dev.streamx.cli.ingestion.publish.payload;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.streamx.cli.exception.PayloadException;
import dev.streamx.cli.exception.ValueException;
import dev.streamx.cli.ingestion.publish.PayloadArgument;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.List;
import org.junit.jupiter.api.Test;

@QuarkusTest
class PayloadResolverTest {

  private static final JsonNode EXAMPLE_JSON_NODE;

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
    assertThat(exception).hasMessageContaining("File do not exists.");
  }

  @Test
  void shouldValidateFileContent() {
    // given
    String corruptedPathArg =
        "file://target/test-classes/dev/streamx/cli/publish/payload/invalid-payload.json";

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
    String arg = "file://target/test-classes/dev/streamx/cli/publish/payload/payload.json";

    // when
    JsonNode payload = createPayload(arg);

    // then
    assertThat(payload).isEqualTo(EXAMPLE_JSON_NODE);
  }

  @Test
  void shouldValidateMissingJsonPathOfNonInitialPayload() {
    // given
    String arg = "file://target/test-classes/dev/streamx/cli/publish/payload/payload.json";

    // when
    Exception exception = catchException(() ->
        cut.createPayload(List.of(
            PayloadArgument.ofString(arg),
            PayloadArgument.ofBinary("$..ana"))
        )
    );

    // then
    assertThat(exception).isInstanceOf(ValueException.class);
  }

  @Test
  void shouldValidateNonexistingJsonPathOfNonInitialPayload() {
    // given
    String arg = "file://target/test-classes/dev/streamx/cli/publish/payload/payload.json";

    // when
    Exception exception = catchException(() ->
        cut.createPayload(List.of(
            PayloadArgument.ofString(arg),
            PayloadArgument.ofBinary("$..ana=lele"))
        )
    );

    // then
    assertThat(exception).isInstanceOf(ValueException.class);
  }

  public JsonNode createPayload(String data) {
    return cut.createPayload(List.of(PayloadArgument.ofString(data)));
  }
}