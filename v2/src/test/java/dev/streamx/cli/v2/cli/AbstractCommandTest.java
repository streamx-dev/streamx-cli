//package dev.streamx.cli.v2.cli;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//import dev.streamx.cli.v2.cli.testing.AbstractTestCommand;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import picocli.CommandLine;
//import picocli.CommandLine.Model.CommandSpec;
//
//import java.io.ByteArrayOutputStream;
//import java.io.PrintStream;
//import java.lang.reflect.Field;
//import java.lang.reflect.Method;
//import java.util.Arrays;
//import java.util.List;
//import java.util.Optional;
//
//class AbstractCommandTest {
//  private final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
//  private final ByteArrayOutputStream errStream = new ByteArrayOutputStream();
//
//  @BeforeEach
//  void redirectStreams() {
//    System.setOut(new PrintStream(outStream));
//    System.setErr(new PrintStream(errStream));
//  }
//
//  @AfterEach
//  void restoreStreams() {
//    System.setOut(System.out);
//    System.setErr(System.err);
//  }
//
//  @Test
//  void runCommand_Success() {
//    CommandResult<String> expectedResult = new CommandResult<>("test data");
//    testCommand.resultToReturn = expectedResult;
//
//    CommandResult<String> result = testCommand.runCommand();
//
//    assertNotNull(result);
//    assertEquals("test data", result.result);
//  }
//
//  @Test
//  void testRunCommand_ThrowsException() {
//    RuntimeException expectedException = new RuntimeException("Test exception");
//    testCommand.exceptionToThrow = expectedException;
//
//    RuntimeException thrown = assertThrows(RuntimeException.class, () -> testCommand.runCommand());
//    assertEquals("Test exception", thrown.getMessage());
//  }
//
//  @Test
//  void testGetHiddenOptions_DefaultEmpty() {
//    List<String> hiddenOptions = testCommand.getHiddenOptions();
//
//    assertNotNull(hiddenOptions);
//    assertTrue(hiddenOptions.isEmpty());
//  }
//
//  @Test
//  void testGetHiddenOptions_CustomList() {
//    List<String> hiddenOptions = testCommandWithHidden.getHiddenOptions();
//
//    assertNotNull(hiddenOptions);
//    assertEquals(2, hiddenOptions.size());
//    assertTrue(hiddenOptions.contains("--output"));
//    assertTrue(hiddenOptions.contains("-o"));
//  }
//
//  @Test
//  void testGetTextOutput_WithNullResult() {
//    TestCommandWithNullResult command = new TestCommandWithNullResult();
//    CommandResult<String> result = new CommandResult<>(null);
//
//    Optional<String> output = command.getTextOutput(result);
//
//    assertTrue(output.isEmpty());
//  }
//
//  @Test
//  void testGetTextOutput_WithResultAsJson() {
//    CommandResult<String> result = new CommandResult<>("test data");
//
//    Optional<String> output = testCommand.getTextOutput(result);
//
//    // Default implementation uses JSON format
//    assertTrue(output.isPresent());
//    String jsonOutput = output.get();
//    assertTrue(jsonOutput.contains("test data"));
//  }
//
//  @Test
//  void testGetTextOutput_CustomImplementation() {
//    TestCommandWithCustomOutput command = new TestCommandWithCustomOutput();
//    CommandResult<String> result = new CommandResult<>("data");
//
//    Optional<String> output = command.getTextOutput(result);
//
//    assertTrue(output.isPresent());
//    assertEquals("Custom output: data", output.get());
//  }
//
//  @Test
//  void testGetTextOutput_WithComplexObject() {
//    TestCommandWithComplexResult command = new TestCommandWithComplexResult();
//    CommandResult<TestDataObject> result = command.runCommand();
//
//    Optional<String> output = command.getTextOutput(result);
//
//    assertTrue(output.isPresent());
//    String jsonOutput = output.get();
//    assertTrue(jsonOutput.contains("test"));
//    assertTrue(jsonOutput.contains("42"));
//  }
//
//  @Test
//  void testPrintUsage_WithCommandLine() {
//    CommandLine cmd = new CommandLine(testCommand);
//
//    testCommand.printUsage();
//
//    String output = outStream.toString();
//    assertTrue(output.length() > 0);
//    assertTrue(output.contains("Usage:") || output.contains("-v"));
//  }
//
//  @Test
//  void testApplyHiddenOptions_ViaCommandLine() {
//    CommandLine cmd = new CommandLine(testCommandWithHidden);
//    CommandSpec spec = cmd.getCommandSpec();
//
//    assertNotNull(spec);
//  }
//
//  @Test
//  void testPromptForInput_WithoutAutocomplete() {
//    // Terminal interaction test - will fail without real terminal
//    assertThrows(RuntimeException.class, () -> {
//      testCommand.promptForInput("Enter value:", null);
//    });
//  }
//
//  @Test
//  void testPromptForInput_WithAutocomplete() {
//    List<String> options = Arrays.asList("option1", "option2", "option3");
//
//    // Terminal interaction test - will fail without real terminal
//    assertThrows(RuntimeException.class, () -> {
//      testCommand.promptForInput("Select:", options);
//    });
//  }
//
//  @Test
//  void testPromptForInput_NullPrompt() {
//    // Test that null prompt is handled
//    assertThrows(RuntimeException.class, () -> {
//      testCommand.promptForInput(null, null);
//    });
//  }
//
//  @Test
//  void testVerboseOption_DefaultFalse() throws Exception {
//    Field verboseField = AbstractCommand.class.getDeclaredField("verbose");
//    verboseField.setAccessible(true);
//
//    boolean verbose = (boolean) verboseField.get(testCommand);
//    assertFalse(verbose);
//  }
//
//  @Test
//  void testOutputFormatOption_DefaultText() throws Exception {
//    Field outputFormatField = AbstractCommand.class.getDeclaredField("outputFormat");
//    outputFormatField.setAccessible(true);
//
//    OutputFormat format = (OutputFormat) outputFormatField.get(testCommand);
//    assertEquals(OutputFormat.text, format);
//  }
//
//  @Test
//  void testCommandLineOptionsPresent() {
//    CommandLine cmd = new CommandLine(testCommand);
//    CommandSpec spec = cmd.getCommandSpec();
//
//    // Verify verbose option exists
//    assertNotNull(spec.findOption("-v"));
//    assertNotNull(spec.findOption("--verbose"));
//
//    // Verify output option exists
//    assertNotNull(spec.findOption("-o"));
//    assertNotNull(spec.findOption("--output"));
//  }
//
//  @Test
//  void testSetOutputFormatViaCommandLine() throws Exception {
//    CommandLine cmd = new CommandLine(testCommand);
//
//    // Parse command line with output format
//    cmd.parseArgs("--output", "json");
//
//    Field outputFormatField = AbstractCommand.class.getDeclaredField("outputFormat");
//    outputFormatField.setAccessible(true);
//
//    OutputFormat format = (OutputFormat) outputFormatField.get(testCommand);
//    assertEquals(OutputFormat.json, format);
//  }
//
//  @Test
//  void testSetVerboseViaCommandLine() throws Exception {
//    CommandLine cmd = new CommandLine(testCommand);
//
//    // Parse command line with verbose flag
//    cmd.parseArgs("--verbose");
//
//    Field verboseField = AbstractCommand.class.getDeclaredField("verbose");
//    verboseField.setAccessible(true);
//
//    boolean verbose = (boolean) verboseField.get(testCommand);
//    assertTrue(verbose);
//  }
//
//  @Test
//  void testSetSpec_InitializesCorrectly() throws Exception {
//    CommandLine cmd = new CommandLine(testCommand);
//
//    Method setSpecMethod = AbstractCommand.class.getDeclaredMethod("setSpec", CommandSpec.class);
//    setSpecMethod.setAccessible(true);
//
//    CommandSpec spec = cmd.getCommandSpec();
//    assertDoesNotThrow(() -> {
//      try {
//        setSpecMethod.invoke(testCommand, spec);
//      } catch (Exception e) {
//        throw new RuntimeException(e);
//      }
//    });
//  }
//
//  @Test
//  void testHiddenOptions_Integration() {
//    CommandLine cmd = new CommandLine(testCommandWithHidden);
//    CommandSpec spec = cmd.getCommandSpec();
//
//    assertNotNull(spec);
//
//    List<String> hiddenOptions = testCommandWithHidden.getHiddenOptions();
//    assertEquals(2, hiddenOptions.size());
//  }
//
//  @Test
//  void testDefaultCommandResult() {
//    AbstractTestCommand command = new AbstractTestCommand();
//    CommandResult<String> result = command.runCommand();
//
//    assertNotNull(result);
//    assertEquals("test result", result.result);
//  }
//
//  @Test
//  void testGetTextOutput_PreservesResultData() {
//    CommandResult<String> result = new CommandResult<>("important data");
//
//    Optional<String> output = testCommand.getTextOutput(result);
//
//    assertTrue(output.isPresent());
//    assertTrue(output.get().contains("important data"));
//  }
//
//  @Test
//  void testCommandResultToText_TextFormat() {
//    TestCommandWithCustomOutput command = new TestCommandWithCustomOutput();
//    CommandResult<String> result = new CommandResult<>("test");
//
//    Optional<String> textOutput = result.toText(OutputFormat.text, command::getTextOutput);
//
//    assertTrue(textOutput.isPresent());
//    assertEquals("Custom output: test", textOutput.get());
//  }
//
//  @Test
//  void testCommandResultToText_JsonFormat() {
//    CommandResult<String> result = new CommandResult<>("test data");
//
//    Optional<String> jsonOutput = result.toText(OutputFormat.json, null);
//
//    assertTrue(jsonOutput.isPresent());
//    assertTrue(jsonOutput.get().contains("test data"));
//  }
//
//  @Test
//  void testCommandResultToText_YamlFormat() {
//    CommandResult<String> result = new CommandResult<>("test data");
//
//    Optional<String> yamlOutput = result.toText(OutputFormat.yaml, null);
//
//    assertTrue(yamlOutput.isPresent());
//    assertTrue(yamlOutput.get().contains("test data"));
//  }
//
//  @Test
//  void testCommandResultToText_ComplexObject() {
//    CommandResult<TestDataObject> result = new CommandResult<>(new TestDataObject("example", 123));
//
//    Optional<String> jsonOutput = result.toText(OutputFormat.json, null);
//
//    assertTrue(jsonOutput.isPresent());
//    String json = jsonOutput.get();
//    assertTrue(json.contains("example"));
//    assertTrue(json.contains("123"));
//  }
//
//  @Test
//  void testShortOptionsWork() throws Exception {
//    CommandLine cmd = new CommandLine(testCommand);
//
//    cmd.parseArgs("-v", "-o", "yaml");
//
//    Field verboseField = AbstractCommand.class.getDeclaredField("verbose");
//    verboseField.setAccessible(true);
//    assertTrue((boolean) verboseField.get(testCommand));
//
//    Field outputFormatField = AbstractCommand.class.getDeclaredField("outputFormat");
//    outputFormatField.setAccessible(true);
//    assertEquals(OutputFormat.yaml, outputFormatField.get(testCommand));
//  }
//
//  @Test
//  void testCommandWithNullResultDoesNotPrintOutput() {
//    TestCommandWithNullResult command = new TestCommandWithNullResult();
//    CommandResult<String> result = command.runCommand();
//
//    Optional<String> output = command.getTextOutput(result);
//
//    assertTrue(output.isEmpty());
//  }
//
//  @Test
//  void testPrintUsageOutputsToSystemOut() {
//    CommandLine cmd = new CommandLine(testCommand);
//
//    testCommand.printUsage();
//
//    String output = outStream.toString();
//    assertFalse(output.isEmpty());
//  }
//
//  record TestDataObject(String name, int value) {
//  }
//}