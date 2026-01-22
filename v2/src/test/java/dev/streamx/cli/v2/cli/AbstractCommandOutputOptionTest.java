package dev.streamx.cli.v2.cli;

import static org.junit.jupiter.api.Assertions.*;

import dev.streamx.cli.v2.cli.testing.AbstractCommandBaseTest;
import dev.streamx.cli.v2.cli.testing.AbstractTestCommand;
import dev.streamx.cli.v2.cli.testing.TestObject;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.util.List;

class AbstractCommandOutputOptionTest extends AbstractCommandBaseTest {
  private final TestObject result = new TestObject(
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

  @Test
  void textOutputFlag_FormatsOutputAsJsonIfNoCustomFormatterProvided() {
    var command = new AbstractTestCommand<>();
    CommandLine commandLine = new CommandLine(command);
    commandLine.parseArgs(CommonOption.OUTPUT_LONG, "text");
    command.setRunCommandHandler(() -> new CommandResult<>(result));
    command.execute();

    var expectedStdOutOutput = """
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
      """.strip() + "\n";

    assertEquals(expectedStdOutOutput, outStream.toString());
    assertEquals("", errStream.toString());
  }

  @Test
  void textOutputFlag_FormatsWithCustomFormatter() {
    var command = new AbstractTestCommand<TestObject>();
    CommandLine commandLine = new CommandLine(command);
    commandLine.parseArgs(CommonOption.OUTPUT_LONG, "text");
    command.setRunCommandHandler(() -> new CommandResult<>(result));
    command.setGetTextOutputHandler((cr) -> """
      String value: %s,
      Total nested objects: %d
      """.formatted(cr.result.stringValue, cr.result.nestedObjects.size()).strip());

    command.execute();

    var expectedStdOutOutput = """
      String value: Test string,
      Total nested objects: 2
      """.strip() + "\n";

    assertEquals(expectedStdOutOutput, outStream.toString());
    assertEquals("", errStream.toString());
  }

  @Test
  void jsonOutputFlag() {
    var command = new AbstractTestCommand<>();
    CommandLine commandLine = new CommandLine(command);
    commandLine.parseArgs(CommonOption.OUTPUT_LONG, "json");
    command.setRunCommandHandler(() -> new CommandResult<>(result));
    command.execute();

    var expectedStdOutOutput = """
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
      """.strip() + "\n";

    assertEquals(expectedStdOutOutput, outStream.toString());
    assertEquals("", errStream.toString());
  }

  @Test
  void yamlOutputFlag() {
    var command = new AbstractTestCommand<TestObject>();
    CommandLine commandLine = new CommandLine(command);
    commandLine.parseArgs(CommonOption.OUTPUT_LONG, "yaml");
    command.setRunCommandHandler(() -> new CommandResult<>(result));
    command.execute();

    var expectedStdOutOutput = """
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
      """.strip() + "\n";

    assertEquals(expectedStdOutOutput, outStream.toString());
    assertEquals("", errStream.toString());
  }

  @Test
  void voidResult() {
    var command = new AbstractTestCommand<Void>();
    command.setRunCommandHandler(() -> new CommandResult<>(null));

    command.output = OutputFormat.text;
    command.execute();

    assertEquals("null\n", outStream.toString());
    assertEquals("", errStream.toString());

    restoreStreams();
    redirectStreams();
    command.output = OutputFormat.json;
    command.execute();

    assertEquals("null\n", outStream.toString());
    assertEquals("", errStream.toString());

    restoreStreams();
    redirectStreams();
    command.output = OutputFormat.yaml;
    command.execute();

    assertEquals("null\n", outStream.toString());
    assertEquals("", errStream.toString());
  }
}