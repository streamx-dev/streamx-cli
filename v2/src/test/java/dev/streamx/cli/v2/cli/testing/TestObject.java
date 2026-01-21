package dev.streamx.cli.v2.cli.testing;

import org.instancio.Instancio;

import java.util.List;

public class TestObject {
  public Void voidValue;
  public boolean booleanValue;
  public long longValue;
  public double floatValue;
  public String stringValue;
  public TestObject nestedObject;
  public List<TestObject> nestedObjects;

  public TestObject(
    Void voidValue,
    boolean booleanValue,
    long longValue,
    double floatValue,
    String stringValue,
    TestObject nestedObject,
    List<TestObject> nestedObjects
  ) {
    this.voidValue = voidValue;
    this.booleanValue = booleanValue;
    this.longValue = longValue;
    this.floatValue = floatValue;
    this.stringValue = stringValue;
    this.nestedObject = nestedObject;
    this.nestedObjects = nestedObjects;
  }

  public static TestObject random() {
    return Instancio.create(TestObject.class);
  }
}