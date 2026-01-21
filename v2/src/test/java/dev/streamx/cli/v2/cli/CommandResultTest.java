package dev.streamx.cli.v2.cli;

import static org.junit.jupiter.api.Assertions.*;

import dev.streamx.cli.v2.cli.testing.TestObject;
import dev.streamx.cli.v2.cli.testing.UnserializableObject;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

class CommandResultTest {
  @Test
  void toText_withTextFormat_shouldUseTextFormatter() {
    var result = new TestObject(
      null,
      true,
      100500,
      42.42,
      "Test string",
      null,
      List.of(TestObject.random(), TestObject.random())
    );

    var commandResult = new CommandResult<>(result);

    Function<CommandResult<TestObject>, Optional<String>> textFormatter =
      cr -> {
        var text = """
          Void Value: %s
          Boolean Value: %b
          Long Value: %d
          Float Value: %.2f
          String Value: %s
          Nested Object: %s
          Total Nested Objects: %d
          """.formatted(
          cr.result.voidValue,
          cr.result.booleanValue,
          cr.result.longValue,
          cr.result.floatValue,
          cr.result.stringValue,
          cr.result.nestedObject,
          cr.result.nestedObjects.size()
        );

        return Optional.of(text);
      };

    var output = commandResult.toText(OutputFormat.text, textFormatter);

    var expectedOutput = """
      Void Value: null
      Boolean Value: true
      Long Value: 100500
      Float Value: 42.42
      String Value: Test string
      Nested Object: null
      Total Nested Objects: 2
      """;

    assertTrue(output.isPresent());
    assertEquals(expectedOutput, output.get());
  }

  @Test
  void toText_withJsonFormat_shouldReturnPrettyPrintedJson() {
    var result = new TestObject(
      null,
      true,
      100500,
      42.42,
      "Test string",
      new TestObject(
        null,
        false,
        7,
        3.14,
        "Nested object test string",
        null,
        null
      ),
      List.of(
        new TestObject(
          null,
          true,
          15,
          100.42,
          "Nested list object 1 test string",
          null,
          null
        ),
        new TestObject(
          null,
          false,
          18,
          0.42,
          "Nested list object 2 test string",
          null,
          null
        )
      )
    );
    var commandResult = new CommandResult<>(result);

    var output = commandResult.toText(OutputFormat.json, null);

    var expectedOutput = """
       {
        "voidValue" : null,
        "booleanValue" : true,
        "longValue" : 100500,
        "floatValue" : 42.42,
        "stringValue" : "Test string",
        "nestedObject" : {
          "voidValue" : null,
          "booleanValue" : false,
          "longValue" : 7,
          "floatValue" : 3.14,
          "stringValue" : "Nested object test string",
          "nestedObject" : null,
          "nestedObjects" : null
        },
        "nestedObjects" : [ {
          "voidValue" : null,
          "booleanValue" : true,
          "longValue" : 15,
          "floatValue" : 100.42,
          "stringValue" : "Nested list object 1 test string",
          "nestedObject" : null,
          "nestedObjects" : null
        }, {
          "voidValue" : null,
          "booleanValue" : false,
          "longValue" : 18,
          "floatValue" : 0.42,
          "stringValue" : "Nested list object 2 test string",
          "nestedObject" : null,
          "nestedObjects" : null
        } ]
      }
      """.strip();

    assertTrue(output.isPresent());
    assertEquals(expectedOutput, output.get());
  }

  @Test
  void toText_withYamlFormat_shouldReturnYaml() {
    var result = new TestObject(
      null,
      true,
      100500,
      42.42,
      "Test string",
      new TestObject(
        null,
        false,
        7,
        3.14,
        "Nested object test string",
        null,
        null
      ),
      List.of(
        new TestObject(
          null,
          true,
          15,
          100.42,
          "Nested list object 1 test string",
          null,
          null
        ),
        new TestObject(
          null,
          false,
          18,
          0.42,
          "Nested list object 2 test string",
          null,
          null
        )
      )
    );
    var commandResult = new CommandResult<>(result);

    var output = commandResult.toText(OutputFormat.yaml, null);

    var expectedOutput = """
      voidValue: null
      booleanValue: true
      longValue: 100500
      floatValue: 42.42
      stringValue: "Test string"
      nestedObject:
        voidValue: null
        booleanValue: false
        longValue: 7
        floatValue: 3.14
        stringValue: "Nested object test string"
        nestedObject: null
        nestedObjects: null
      nestedObjects:
      - voidValue: null
        booleanValue: true
        longValue: 15
        floatValue: 100.42
        stringValue: "Nested list object 1 test string"
        nestedObject: null
        nestedObjects: null
      - voidValue: null
        booleanValue: false
        longValue: 18
        floatValue: 0.42
        stringValue: "Nested list object 2 test string"
        nestedObject: null
        nestedObjects: null
      """.strip();

    assertTrue(output.isPresent());
    assertEquals(expectedOutput, output.get());
  }

  @Test
  void toText_withNullResult_shouldHandleGracefully() {
    CommandResult<TestObject> commandResult = new CommandResult<>(null);

    assertEquals("null", commandResult.toText(OutputFormat.json, null).get());
    assertEquals("null", commandResult.toText(OutputFormat.yaml, null).get());
  }

  @Test
  void toText_shouldThrowRuntimeExceptionForUnserializableObject() {
    UnserializableObject unserializable = new UnserializableObject();
    CommandResult<UnserializableObject> commandResult = new CommandResult<>(unserializable);

    assertThrows(RuntimeException.class, () ->
      commandResult.toText(OutputFormat.json, null)
    );
  }
}