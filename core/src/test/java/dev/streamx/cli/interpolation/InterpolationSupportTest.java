package dev.streamx.cli.interpolation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.quarkus.test.component.QuarkusComponentTest;
import jakarta.inject.Inject;
import java.util.NoSuchElementException;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

@QuarkusComponentTest
public class InterpolationSupportTest {

  @Inject
  InterpolationSupport interpolationSupport;

  @AfterEach
  void cleanup() {
    var propertiesToClear = Set.of(
        "this.is.a.property", "prop1", "prop2", "nested", "inner", "whitespace.property");

    propertiesToClear.forEach(System::clearProperty);
  }

  @Test
  void testExpandWithValidProperty() {
    // Setup: Define the environment or system properties
    System.setProperty("this.is.a.property", "validValue");

    // Valid case
    String rawValue = "${this.is.a.property}";
    String result = interpolationSupport.expand(rawValue);

    assertEquals("validValue", result);
  }

  @Test
  void testExpandWithMultipleConfigSources() {
    // Setup: Define the environment or system properties
    System.setProperty("this.is.a.property", "From application.properties ->");

    // Valid case
    String rawValue = "${this.is.a.property} ${streamx.cli.settings.root-dir}";
    String result = interpolationSupport.expand(rawValue);

    assertEquals("From application.properties -> ./target/test-classes/dev.streamx.cli.settings",
        result);
  }

  @Test
  void testExpandWithMultipleProperties() {
    // Setup multiple properties
    System.setProperty("prop1", "value1");
    System.setProperty("prop2", "value2");

    String rawValue = "Property1: ${prop1}, Property2: ${prop2}";
    String result = interpolationSupport.expand(rawValue);

    assertEquals("Property1: value1, Property2: value2", result);
  }

  @Test
  void testExpandWithNestedProperties() {
    // Setup nested properties
    System.setProperty("nested", "${inner}");
    System.setProperty("inner", "finalValue");

    String rawValue = "${nested}";
    String result = interpolationSupport.expand(rawValue);

    assertEquals("finalValue", result);
  }

  @Test
  void testExpandWithMissingProperty() {
    // Case where property is not defined
    String rawValue = "${undefined.property}";

    assertThrows(NoSuchElementException.class,
        () -> interpolationSupport.expand(rawValue),
        "Could not expand value undefined.property in expression ${undefined.property}");
  }

  @Test
  void testExpandWithPartialProperty() {
    System.setProperty("this.is.a.property", "validValueToo");

    // Case with partially closed property
    String rawValue = "${this.is.a.property"; // Missing closing brace

    String result = interpolationSupport.expand(rawValue);

    assertEquals("validValueToo", result);
  }

  @Test
  void testExpandWithNoInterpolation() {
    // Case with no interpolation markers
    String rawValue = "No interpolation here.";
    String result = interpolationSupport.expand(rawValue);

    assertEquals("No interpolation here.", result);
  }

  @Test
  void testExpandWithEmptyString() {
    // Case with empty string
    String rawValue = "";
    String result = interpolationSupport.expand(rawValue);

    assertEquals("", result);
  }

  @Test
  void testExpandWithNullInput() {
    // Case with null input
    String rawValue = null;

    Exception exception = assertThrows(NullPointerException.class,
        () -> interpolationSupport.expand(rawValue), "Expression cannot be null");
  }

  @Test
  void testExpandWithEscapedPropertySyntax() {
    // Case with escaped property syntax
    String rawValue = "\\${this.is.a.property}"; // Escaped $ to avoid interpolation
    String result = interpolationSupport.expand(rawValue);

    assertEquals("${this.is.a.property}", result);
  }

  @Test
  void testExpandWithWhitespaceAroundProperty() {
    // Case with whitespace
    System.setProperty("whitespace.property", "value");

    String rawValue = "   ${whitespace.property}   ";
    String result = interpolationSupport.expand(rawValue);

    assertEquals("   value   ", result, "Whitespace outside property markers should be preserved");
  }
}
