package dev.streamx.cli.ingestion.publish.payload.typed;

public enum SourceType {
  JSON(true),
  STRING(false),
  BINARY(false);

  private final boolean mergeAllowed;

  SourceType(boolean mergeAllowed) {
    this.mergeAllowed = mergeAllowed;
  }

  public boolean isMergeAllowed() {
    return mergeAllowed;
  }
}
