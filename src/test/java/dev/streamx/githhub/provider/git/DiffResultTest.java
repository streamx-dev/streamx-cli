package dev.streamx.githhub.provider.git;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.junit.jupiter.api.Test;

class DiffResultTest {


  @Test
  public void testShouldAddOldPathWhenChangeTypeDelete() {
    DiffEntry deleteEntry = mock(DiffEntry.class);
    when(deleteEntry.getChangeType()).thenReturn(ChangeType.DELETE);
    when(deleteEntry.getOldPath()).thenReturn("styles/main.css");
    when(deleteEntry.getNewPath()).thenReturn("/dev/null");

    DiffResult diffResult = new DiffResult();
    diffResult.add(deleteEntry);

    assertTrue(diffResult.getDeletedPaths().contains("styles/main.css"));
    assertTrue(diffResult.getModifiedPaths().isEmpty());
  }

  @Test
  public void testShouldAddNewPathWhenChangeTypeIsNotDelete() {
    DiffEntry modifiedEntry = mock(DiffEntry.class);
    when(modifiedEntry.getChangeType()).thenReturn(ChangeType.MODIFY);
    when(modifiedEntry.getOldPath()).thenReturn("styles/main.css");
    when(modifiedEntry.getNewPath()).thenReturn("styles/main.css");

    DiffResult diffResult = new DiffResult();
    diffResult.add(modifiedEntry);

    assertTrue(diffResult.getModifiedPaths().contains("styles/main.css"));
    assertTrue(diffResult.getDeletedPaths().isEmpty());
  }

}