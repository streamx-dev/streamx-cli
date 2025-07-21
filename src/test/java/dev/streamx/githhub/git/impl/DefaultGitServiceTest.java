package dev.streamx.githhub.git.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.streamx.exception.GitHubActionException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collection;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.RmCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ReflogEntry;
import org.eclipse.jgit.lib.Repository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DefaultGitServiceTest {

  @TempDir
  static Path tempDir;

  private Path gitDir;

  private Git git;

  private Repository repository;

  private DefaultGitService service;

  @BeforeEach
  public void setUp() throws GitAPIException, IOException {
    gitDir = tempDir.resolve(".git");

    service = new DefaultGitService();
    git = Git.init().setDirectory(tempDir.toFile()).setGitDir(gitDir.toFile()).call();
    repository = git.getRepository();
  }

  @AfterEach
  public void after() throws IOException {
    repository.close();
    git.close();
    FileUtils.deleteDirectory(tempDir.toFile());
  }

  @Test
  public void testShouldReturnEmptyResult() throws GitHubActionException {
    DiffResult diffResult = service.getDiff(tempDir.toString(), 0);
    assertEquals(DiffResult.EMPTY_RESULT, diffResult);
  }

  @Test
  public void testShouldReturnCommitChanges()
      throws GitHubActionException, GitAPIException, IOException {
    Path file1 = createTestFile("file1.txt");
    Path file2 = createTestFile("file2.txt");

    // given
    AddCommand addCommand = git.add();
    addCommand.addFilepattern(file1.getFileName().toString()).call();
    git.commit().setMessage("initial commit").setAll(true).call();

    AddCommand add2 = git.add();
    add2.addFilepattern(file2.getFileName().toString()).call();
    git.commit().setMessage("change commit").setAll(true).call();

    Collection<ReflogEntry> reflogEntries = git.reflog().call();
    assertEquals(2, reflogEntries.size());

    // when
    DiffResult diffResult = service.getDiff(tempDir.toString(), 1);

    // then
    assertNotEquals(DiffResult.EMPTY_RESULT, diffResult);
    assertFalse(diffResult.getModifiedPaths().isEmpty());
    assertTrue(diffResult.getDeletedPaths().isEmpty());
  }

  private Path createTestFile(String fileName) throws IOException {
    Path file = tempDir.resolve(fileName);
    FileUtils.write(file.toFile(), fileName + " test content",  StandardCharsets.UTF_8);
    return file;
  }


  @Test
  public void testShouldReturnDiffResultWithDeletedEntry()
      throws GitHubActionException, GitAPIException, IOException {
    // given
    Path file1 = createTestFile("file1.txt");
    Path file2 = createTestFile("file2.txt");

    AddCommand addCommand = git.add();
    addCommand.addFilepattern(file1.getFileName().toString()).call();
    git.commit().setMessage("initial commit").setAll(true).call();

    AddCommand addCommand2 = git.add();
    addCommand2.addFilepattern(file2.getFileName().toString()).call();
    git.commit().setMessage("change commit").setAll(true).call();

    RmCommand rmCommand = git.rm();
    rmCommand.addFilepattern(file2.getFileName().toString()).call();
    git.commit().setMessage("delete commit").setAll(true).call();

    Collection<ReflogEntry> reflogEntries = git.reflog().call();
    assertEquals(3, reflogEntries.size());
    // when
    DiffResult diffResult = service.getDiff(tempDir.toString(), 1);
    // then
    assertNotEquals(DiffResult.EMPTY_RESULT, diffResult);
    assertTrue(diffResult.getModifiedPaths().isEmpty());
    assertFalse(diffResult.getDeletedPaths().isEmpty());
  }


}