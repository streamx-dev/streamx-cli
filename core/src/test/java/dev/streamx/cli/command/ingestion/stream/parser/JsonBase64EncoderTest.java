package dev.streamx.cli.command.ingestion.stream.parser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class JsonBase64EncoderTest {

  private static final ObjectMapper objectMapper = new ObjectMapper();
  public static final String INITIAL_JSON = """
      {
        "foo" : {
          "bar" : "foobar"
        },
        "foo2" : "bar2"
      }""";

  static Stream<Arguments> testArguments() {
    return Stream.of(
        // happy path
        arguments(
            INITIAL_JSON,
            "/foo/bar",
            INITIAL_JSON.replace("foobar", "Zm9vYmFy")
        ),

        // missing leading slash in the jsonFieldsAsBase64 should be automatically added
        arguments(
            INITIAL_JSON,
            "foo/bar",
            INITIAL_JSON.replace("foobar", "Zm9vYmFy")
        ),

        // attempting to encode a parent node that is not a text node
        arguments(
            INITIAL_JSON,
            "/foo",
            INITIAL_JSON
        ),

        // json doesn't contain the requested field
        arguments(
            INITIAL_JSON,
            "bar",
            INITIAL_JSON
        ),

        // invalid path of field
        arguments(
            INITIAL_JSON,
            "/a/)#Q(*%Y@W$Tbngow23p0t[';'\\325/b",
            INITIAL_JSON
        )
    );
  }

  @ParameterizedTest
  @MethodSource("testArguments")
  void shouldEncodeFields(String sourceJson, String jsonFieldsAsBase64, String expectedResultJson)
      throws Exception {
    // given
    JsonNode jsonNode = objectMapper.readTree(sourceJson);

    // when
    JsonBase64Encoder.encodeFields(jsonNode, List.of(jsonFieldsAsBase64));

    // then
    assertThat(jsonNode.toPrettyString()).isEqualTo(expectedResultJson);
  }

}