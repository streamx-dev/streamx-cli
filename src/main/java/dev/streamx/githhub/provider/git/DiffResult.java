package dev.streamx.githhub.provider.git;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;

public class DiffResult {

  public static final DiffResult EMPTY_RESULT = new DiffResult();

  private final Set<String> updated = new HashSet<>();

  private final Set<String> deleted = new HashSet<>();

  void add(DiffEntry entry) {
    Optional.ofNullable(entry)
        .ifPresent(e -> {
          if (ChangeType.DELETE.equals(e.getChangeType())) {
            deleted.add(e.getOldPath());
          } else {
            updated.add(e.getNewPath());
          }
        });
  }

  public boolean isEmpty() {
    return updated.isEmpty() && deleted.isEmpty();
  }

  public Set<String> getModifiedPaths() {
    return Collections.unmodifiableSet(updated);
  }

  public Set<String> getDeletedPaths() {
    return Collections.unmodifiableSet(deleted);
  }

}
