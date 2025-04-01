package dev.streamx.cli.exception;

public class GitException extends RuntimeException {

  private final Process process;

  private GitException(String message, Process process) {
    super(message);
    this.process = process;
  }

  public static GitException gitCloneException(Process process) {
    return new GitException("git clone failed.",
        process);
  }

  public static GitException gitNotInstalledException(Process process) {
    return new GitException("""
        Could not find a Git executable.

        Make sure that:
         * Git is installed,
         * Git is available on $PATH""",
        process);
  }

  public Process getProcess() {
    return process;
  }
}
