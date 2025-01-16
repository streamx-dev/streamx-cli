package dev.streamx.cli.interpolation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.org.bouncycastle.oer.its.etsi102941.Url;

@QuarkusTest
public class InterpolatingMapperTest {

  @Inject
  @Interpolating
  ObjectMapper mapper;

  @Test
  void testInterpolateStringFields() throws Exception {
    System.setProperty("test.interpolatedValue", "interpolatedValue");
    String json = "{\"field\":\"${test.interpolatedValue}\"}";

    TestClass result = mapper.readValue(json, TestClass.class);

    assertEquals("interpolatedValue", result.getField());
  }

  @Test
  void testNestedInterpolation() throws Exception {
    System.setProperty("test.nestedValue", "nestedValue");
    String json = "{\"nested\": {\"field\": \"${test.nestedValue}\"}}";

    NestedTestClass result = mapper.readValue(json, NestedTestClass.class);

    assertEquals("nestedValue", result.getNested().getField());
  }

  @Test
  void testArrayInterpolation() throws Exception {
    System.setProperty("test.arrayValue1", "interpolatedValue1");
    System.setProperty("test.arrayValue2", "interpolatedValue2");
    String json = "[\"${test.arrayValue1}\", \"${test.arrayValue2}\"]";

    String[] result = mapper.readValue(json, String[].class);

    assertEquals("interpolatedValue1", result[0]);
    assertEquals("interpolatedValue2", result[1]);
  }

  @Test
  void testMixedTypesInterpolation() throws Exception {
    System.setProperty("test.string", "interpolatedValue");
    System.setProperty("test.int", "-1");
    System.setProperty("test.boolean", "true");

    String json = """
        {
          "bool": true,
          "string": "${test.string}",
          "aLong": "${test.int}",
          "integer": "${test.int}",
          "url": "http://${test.string}"
        }
        """;
    MixedTypesTestClass result = mapper.readValue(json, MixedTypesTestClass.class);
    assertTrue(result.isBool());
    assertEquals("interpolatedValue", result.getString());
    assertEquals(-1, result.getaLong());
    assertEquals(-1, result.getInteger());
    assertEquals("http://interpolatedValue", result.getUrl().getUrl());
  }


  static class MixedTypesTestClass {

    private boolean bool;
    private String string;
    private Integer integer;
    private Long aLong;
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

    public Long getaLong() {
      return aLong;
    }

    public void setaLong(Long aLong) {
      this.aLong = aLong;
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
