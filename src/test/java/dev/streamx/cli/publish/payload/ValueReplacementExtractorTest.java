package dev.streamx.cli.publish.payload;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchException;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.jayway.jsonpath.JsonPath;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class ValueReplacementExtractorTest {

  ValueReplacementExtractor cut = new ValueReplacementExtractor();

  @ParameterizedTest
  @ValueSource(strings = {
      "nanaLele",
      "=",
      "$..book[?(@.price <= $['expensive'])]"
  })
  void shouldNotExtractPair(String arg) {
    // when
    Exception exception = catchException(() -> cut.extract(arg));

    // then
    assertThat(exception)
        .isInstanceOf(ValueException.class)
        .hasMessageContaining("Could not extract jsonPath from arg");
  }

  @ParameterizedTest
  @MethodSource("extractPair")
  void shouldExtractPair(String arg, JsonPath key, String newValue) {
    // when
    var result = cut.extract(arg);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getKey().getPath()).isEqualTo(key.getPath());
    assertThat(result.getValue()).isEqualTo(newValue);
  }

  static Stream<Arguments> extractPair() {
    return Stream.of(
        arguments("nana.lele=newValue", JsonPath.compile("nana.lele"), "newValue"),
        arguments("$..book[?(@.author =~ /.*REES/i)]=newValue", JsonPath.compile("$..book[?(@.author =~ /.*REES/i)]"), "newValue"),
        arguments("jsonPath=", JsonPath.compile("jsonPath"), ""),
        arguments("$..*[?(@.*=='=')]===", JsonPath.compile("$..*[?(@.*=='=')]"), "=="),
        arguments("===", JsonPath.compile("="), "=")
    );
  }
}