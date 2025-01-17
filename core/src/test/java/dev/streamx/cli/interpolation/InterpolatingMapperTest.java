package dev.streamx.cli.interpolation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.org.bouncycastle.oer.its.etsi102941.Url;

@QuarkusTest
public class InterpolatingMapperTest {

  @Inject
  @Interpolating
  ObjectMapper mapper;

  @AfterEach
  void cleanup() {
    var propertiesToClear = Set.of("test.interpolatedValue", "test.nestedValue",
        "test.arrayValue1", "test.arrayValue2", "test.string", "test.int", "test.boolean");

    propertiesToClear.forEach(System::clearProperty);
  }

  @Test
  void testInterpolateStringFields() throws Exception {
    System.setProperty("test.interpolatedValue", "interpolatedValue");
    String yaml = """
        field: ${test.interpolatedValue}
        """;
    TestClass result = mapper.readValue(yaml, TestClass.class);

    assertEquals("interpolatedValue", result.getField());
  }

  @Test
  void testInterpolateFieldWithDefault() throws Exception {
    String yaml = """
        field: ${test.interpolatedValue:default}
        """;
    TestClass result = mapper.readValue(yaml, TestClass.class);

    assertEquals("default", result.getField());
  }

  @Test
  void testNotInterpolatingNullField() throws Exception {
    String yaml = """
        field: null
        """;
    TestClass result = mapper.readValue(yaml, TestClass.class);

    assertNull(result.getField());
  }

  @Test
  void testNotInterpolatingFieldWithoutPlaceholder() throws Exception {
    String yaml = """
        field: "field"
        """;
    TestClass result = mapper.readValue(yaml, TestClass.class);

    assertEquals("field", result.getField());
  }

  @Test
  void testNestedInterpolation() throws Exception {
    System.setProperty("test.nestedValue", "nestedValue");
    String yaml = """
        nested:
          field: ${test.nestedValue}
        """;

    NestedTestClass result = mapper.readValue(yaml, NestedTestClass.class);

    assertEquals("nestedValue", result.getNested().getField());
  }

  @Test
  void testArrayInterpolation() throws Exception {
    System.setProperty("test.arrayValue1", "interpolatedValue1");
    System.setProperty("test.arrayValue2", "interpolatedValue2");

    String yaml = """
        - "${test.arrayValue1}"
        - "${test.arrayValue2}"
        """;
    String[] result = mapper.readValue(yaml, String[].class);

    assertEquals("interpolatedValue1", result[0]);
    assertEquals("interpolatedValue2", result[1]);
  }

  @Test
  void testMixedTypesInterpolation() throws Exception {
    System.setProperty("test.string", "interpolatedValue");
    System.setProperty("test.int", "-1");
    System.setProperty("test.boolean", "true");

    String yaml = """
          bool: true
          string: ${test.string}
          longField: ${test.int}
          integer: ${test.int}
          url: http://${test.string}
        """;
    MixedTypesTestClass result = mapper.readValue(yaml, MixedTypesTestClass.class);
    assertTrue(result.isBool());
    assertEquals("interpolatedValue", result.getString());
    assertEquals(-1, result.getLongField());
    assertEquals(-1, result.getInteger());
    assertEquals("http://interpolatedValue", result.getUrl().getUrl());
  }


  static class MixedTypesTestClass {

    private boolean bool;
    private String string;
    private Integer integer;
    private Long longField;
    private Url url;

    public boolean isBool() {
      return bool;
    }

    public void setBool(boolean bool) {
      this.bool = bool;
    }

    public String getString() {
      return string;
    }

    public void setString(String string) {
      this.string = string;
    }

    public Integer getInteger() {
      return integer;
    }

    public void setInteger(Integer integer) {
      this.integer = integer;
    }

    public Long getLongField() {
      return longField;
    }

    public void setLongField(Long longField) {
      this.longField = longField;
    }

    public Url getUrl() {
      return url;
    }

    public void setUrl(Url url) {
      this.url = url;
    }
  }

  static class TestClass {

    private String field;

    public TestClass() {
    }

    public TestClass(String field) {
      this.field = field;
    }

    public String getField() {
      return field;
    }

    public void setField(String field) {
      this.field = field;
    }
  }

  static class NestedTestClass {

    private TestClass nested;

    public NestedTestClass() {
    }

    public NestedTestClass(TestClass nested) {
      this.nested = nested;
    }

    public TestClass getNested() {
      return nested;
    }

    public void setNested(TestClass nested) {
      this.nested = nested;
    }
  }
}
