package dev.streamx.githhub.utils;

import dev.streamx.exception.GitHubActionException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class FilesUtils {

  private static final String DEFAULT_PATH_MATCHER_SYNTAX = "glob:";

  public static Set<String> listFiles(String workspace) throws GitHubActionException {
    try {
      return Files.find(Paths.get(workspace),
              Integer.MAX_VALUE,
              (filePath, fileAttr) -> fileAttr.isRegularFile())
          .map(fileAttr -> fileAttr.toAbsolutePath().toString())
          .map(path -> path.substring(workspace.length() + 1))
          .collect(Collectors.toSet());
    } catch (IOException exc) {
      throw new GitHubActionException(exc.getMessage(), exc);
    }
  }

  public static Set<String> listFilteredFiles(String workspace, String... filters)
      throws GitHubActionException {
    try {
      return Files.find(Paths.get(workspace),
              Integer.MAX_VALUE,
              (filePath, fileAttr) -> fileAttr.isRegularFile())
          .map(filePath -> filePath.toAbsolutePath().toString())
          .map(path -> path.substring(workspace.length() + 1))
          .filter(path -> validatePathMatches(path, filters))
          .collect(Collectors.toSet());
    } catch (IOException exc) {
      throw new GitHubActionException(exc.getMessage(), exc);
    }
  }

  public static boolean isValidPath(String path, String... filters) {
    return Optional.ofNullable(path)
        .filter(p -> validatePathMatches(p, filters))
        .isPresent();
  }

  private static boolean validatePathMatches(String path, String[] filters) {
    Path filePath = Path.of(path);
    for (String filter : filters) {
      PathMatcher matcher = filePath.getFileSystem()
          .getPathMatcher(DEFAULT_PATH_MATCHER_SYNTAX + filter);
      if (matcher.matches(filePath)) {
        return true;
      }
    }
    return false;
  }


}
