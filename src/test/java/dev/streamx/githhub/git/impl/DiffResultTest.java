package dev.streamx.githhub.git.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.junit.jupiter.api.Test;

class DiffResultTest {


  @Test
  public void testShouldAddOldPathWhenChangeTypeDelete() {
      DiffResult diffResult = new DiffResult();
      DiffEntry deleteEntry = mock(DiffEntry.class);
      when(deleteEntry.getChangeType()).thenReturn(ChangeType.DELETE);
      when(deleteEntry.getOldPath()).thenReturn("styles/main.css");
      when(deleteEntry.getNewPath()).thenReturn("/dev/null");

      diffResult.add(deleteEntry);

      assertTrue(diffResult.getDeletedPaths().contains("styles/main.css"));
      assertTrue(diffResult.getModifiedPaths().isEmpty());
  }

  @Test
  public void testShouldAddNewPathWhenChangeTypeIsNotDelete() {
    DiffResult diffResult = new DiffResult();
    DiffEntry modifiedEntry = mock(DiffEntry.class);
    when(modifiedEntry.getChangeType()).thenReturn(ChangeType.MODIFY);
    when(modifiedEntry.getOldPath()).thenReturn("styles/main.css");
    when(modifiedEntry.getNewPath()).thenReturn("styles/main.css");

    diffResult.add(modifiedEntry);

    assertTrue(diffResult.getModifiedPaths().contains("styles/main.css"));
    assertTrue(diffResult.getDeletedPaths().isEmpty());
  }

}