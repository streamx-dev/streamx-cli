package dev.streamx.cli.v2.cli;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.function.Function;

class CommandResultTest {
  @Test
  void toText_withTextFormat_shouldUseTextFormatter() {
    var result = new ComplexTestObject(
      new TestResultObject("example", 42, true),
      new String[]{"a", "b"}
    );
    var commandResult = new CommandResult<>(result);

    Function<CommandResult<ComplexTestObject>, Optional<String>> textFormatter =
      cr -> {
        var text = """
          Name: %s
          Value: %d
          Total items: %d
          """.formatted(cr.result.nested.name, cr.result.nested.value, cr.result.items.length);

        return Optional.of(text);
      };

    var output = commandResult.toText(OutputFormat.text, textFormatter);

    var expectedOutput = """
      Name: example
      Value: 42
      Total items: 2
      """;

    assertTrue(output.isPresent());
    assertEquals(expectedOutput, output.get());
  }

  @Test
  void toText_withJsonFormat_shouldReturnPrettyPrintedJson() {
    var result = new ComplexTestObject(
      new TestResultObject("example", 42, true),
      new String[]{"a", "b"}
    );
    var commandResult = new CommandResult<>(result);

    var output = commandResult.toText(OutputFormat.json, null);

    var expectedOutput = """
      {
        "nested" : {
          "name" : "example",
          "value" : 42,
          "active" : true
        },
        "items" : [ "a", "b" ]
      }
      """.strip();

    assertTrue(output.isPresent());
    assertEquals(expectedOutput, output.get());
  }

  @Test
  void toText_withYamlFormat_shouldReturnYaml() {
    var result = new ComplexTestObject(
      new TestResultObject("example", 42, true),
      new String[]{"a", "b"}
    );
    var commandResult = new CommandResult<>(result);

    var output = commandResult.toText(OutputFormat.yaml, null);

    var expectedOutput = """
      ---
      nested:
        name: "example"
        value: 42
        active: true
      items:
      - "a"
      - "b"
      """;

    assertTrue(output.isPresent());
    assertEquals(expectedOutput, output.get());
  }

  @Test
  void toText_withNullResult_shouldHandleGracefully() {
    CommandResult<TestResultObject> commandResult = new CommandResult<>(null);

    assertEquals("null", commandResult.toText(OutputFormat.json, null).get());
    assertEquals("--- null\n", commandResult.toText(OutputFormat.yaml, null).get());
  }

  @Test
  void toText_shouldThrowRuntimeExceptionForUnserializableObject() {
    UnserializableObject unserializable = new UnserializableObject();
    CommandResult<UnserializableObject> commandResult = new CommandResult<>(unserializable);

    assertThrows(RuntimeException.class, () ->
      commandResult.toText(OutputFormat.json, null)
    );
  }

  static class TestResultObject {
    public String name;
    public int value;
    public boolean active;

    public TestResultObject(String name, int value, boolean active) {
      this.name = name;
      this.value = value;
      this.active = active;
    }
  }

  static class ComplexTestObject {
    public TestResultObject nested;
    public String[] items;

    public ComplexTestObject(TestResultObject nested, String[] items) {
      this.nested = nested;
      this.items = items;
    }
  }

  static class UnserializableObject {
    // Object with circular reference to make it unserializable
    public UnserializableObject self = this;
  }
}