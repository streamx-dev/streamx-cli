package dev.streamx.githhub.utils;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.streamx.exception.GitHubActionException;
import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.Set;
import org.junit.jupiter.api.Test;

class FilesUtilsTest {

  @Test
  public void testShouldListAllFiles() throws GitHubActionException {
    Path testWorkspacePath = getTestWorkspacePath();

    Set<String> result = FilesUtils.listFiles(testWorkspacePath.toAbsolutePath().toString());

    assertFalse(result.isEmpty());
    assertEquals(6, result.size());
    assertTrue(result.stream().allMatch(s -> s.contains(".css") || s.contains(".js")));
    assertTrue(result.contains("root.css"));
    assertTrue(result.contains("test_1/file_1_1.css"));
    assertTrue(result.contains("test_1/file_1_2.css"));
    assertTrue(result.contains("test_2/file_2_1.css"));
    assertTrue(result.contains("test_2/file_2_1.js"));
    assertTrue(result.contains("test_2/test_2_2/file_2_2_1.js"));
  }

  @Test
  public void testShouldListOnlyCssFiles() throws GitHubActionException {
    Path testWorkspacePath = getTestWorkspacePath();

    Set<String> result = FilesUtils.listFilteredFiles(
        testWorkspacePath.toAbsolutePath().toString(), "**.css");

    assertFalse(result.isEmpty());
    assertEquals(4, result.size());
    assertTrue(result.stream().allMatch(s -> s.contains(".css")));
    assertTrue(result.contains("root.css"));
    assertTrue(result.contains("test_1/file_1_1.css"));
    assertTrue(result.contains("test_1/file_1_2.css"));
    assertTrue(result.contains("test_2/file_2_1.css"));
  }

  @Test
  public void testShouldListOnlyTest1CssFiles() throws GitHubActionException {
    Path testWorkspacePath = getTestWorkspacePath();

    Set<String> result = FilesUtils.listFilteredFiles(
        testWorkspacePath.toAbsolutePath().toString(), "test_1/*.css");

    assertFalse(result.isEmpty());
    assertEquals(2, result.size());
    assertTrue(result.stream().allMatch(s -> s.contains(".css")));
    assertTrue(result.contains("test_1/file_1_1.css"));
    assertTrue(result.contains("test_1/file_1_2.css"));
  }

  @Test
  public void testShouldListOnlyJsFiles() throws GitHubActionException {
    Path testWorkspacePath = getTestWorkspacePath();

    Set<String> result = FilesUtils.listFilteredFiles(
        testWorkspacePath.toAbsolutePath().toString(), "**.js");

    assertFalse(result.isEmpty());
    assertEquals(2, result.size());
    assertTrue(result.stream().allMatch(s -> s.contains(".js")));
  }

  @Test
  public void testShouldListOnlyCssFilesFromOneFilter() throws GitHubActionException {
    Path testWorkspacePath = getTestWorkspacePath();

    Set<String> result = FilesUtils.listFilteredFiles(
        testWorkspacePath.toAbsolutePath().toString(), "test_2/*.css");

    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    assertTrue(result.stream().allMatch(s -> s.contains(".css")));
  }

  @Test
  public void testShouldListOnlyJsFilesFromOneFilter() throws GitHubActionException {
    Path testWorkspacePath = getTestWorkspacePath();

    Set<String> result = FilesUtils.listFilteredFiles(
        testWorkspacePath.toAbsolutePath().toString(), "test_2/*.js", "test_2/**/*.js");

    assertFalse(result.isEmpty());
    assertEquals(2, result.size());
    assertTrue(result.stream().allMatch(s -> s.contains(".js")));
  }

  @Test
  public void testShouldValidatePathMatches() {
    assertTrue(FilesUtils.isValidPath("test_2/file_2_1.css", "test_2/*.css"));
  }

  private Path getTestWorkspacePath() {
    File testWorkspace = new File("src/test/resources/dev/streamx/github/files");
    return Path.of(testWorkspace.getAbsolutePath());
  }
}