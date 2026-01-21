package dev.streamx.cli.v2.cli.testing;

public class UnserializableObject {
  // Object with circular reference to make it unserializable
  public UnserializableObject self = this;
}